package io.zensoft.web.handler

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled.*
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.netty.util.AsciiString
import io.zensoft.web.exception.HandlerMethodNotFoundException
import io.zensoft.web.provider.ExceptionHandlerProvider
import io.zensoft.web.provider.HandlerParameterMapperProvider
import io.zensoft.web.provider.MethodHandlerProvider
import io.zensoft.web.provider.ResponseResolverProvider
import io.zensoft.web.support.*
import io.zensoft.web.support.WrappedHttpRequest
import io.zensoft.web.support.HttpResponse
import io.zensoft.web.support.MimeType.*
import io.zensoft.web.utils.DeserializationUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.reflect.InvocationTargetException

@ChannelHandler.Sharable
@Component
class HttpControllerHandler(
    private val pathHandlerProvider: MethodHandlerProvider,
    private val exceptionHandlerProvider: ExceptionHandlerProvider,
    private val sessionStorage: SessionStorage,
    private val sessionHandler: SessionHandler,
    private val handlerParameterProvider: HandlerParameterMapperProvider,
    private val responseResolverProvider: ResponseResolverProvider
) : SimpleChannelInboundHandler<FullHttpRequest>() {

    companion object {
        private const val REDIRECT_PREFIX = "redirect:"

        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        val response = HttpResponse()
        val wrappedRequest = WrappedHttpRequest.wrap(request)
        var handler: HttpHandlerMetaInfo? = null
        try {
            handler = pathHandlerProvider.getMethodHandler(wrappedRequest.path, wrappedRequest.method)
            handleRequest(handler, wrappedRequest, response)
        } catch (ex: InvocationTargetException) {
            handleException(handler!!.contentType, ex.targetException!!, wrappedRequest, response)
        } catch (ex: HandlerMethodNotFoundException) {
            handleException(TEXT_HTML, ex, wrappedRequest, response)
        } catch (ex: Exception) {
            handleException(handler!!.contentType, ex, wrappedRequest, response)
        }

        writeResponse(ctx, request, response)
    }

    /**
     * Marked deprecated as method moved from [io.netty.channel.ChannelHandler] to it's child interface [io.netty.channel.ChannelInboundHandler]
     */
    @Throws(Exception::class)
    @Suppress("OverridingDeprecatedMember")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Something went wrong", cause)
        writeResponse(ctx, null, HttpResponse(TEXT_PLAIN, INTERNAL_SERVER_ERROR, toByteBuf(cause.message ?: "")))
    }

    private fun writeResponse(ctx: ChannelHandlerContext, request: FullHttpRequest?, response: HttpResponse) {
        val buf = response.content
        val result = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, response.status, buf)
        for (header in response.headers) {
            result.headers().set(header.key, header.value)
        }
        if (buf != EMPTY_BUFFER) {
            result.headers().set(HttpHeaderNames.CONTENT_TYPE, response.mimeType!!.value.toString() + "; charset=UTF-8")
        }
        result.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf!!.readableBytes())
        HttpUtil.setKeepAlive(result, HttpUtil.isKeepAlive(request))

        ctx.writeAndFlush(result)
    }

    private fun handleRequest(handler: HttpHandlerMetaInfo, request: WrappedHttpRequest, response: HttpResponse) {
        if (!handler.stateless) sessionHandler.handleSession(request.originalRequest, response)
        val args = createHandlerArguments(handler, request)
        val result = handler.execute(*args)
        if(result is String && result.startsWith(REDIRECT_PREFIX)) {
            response.modify(HttpStatus.FOUND.value, handler.contentType, toByteBuf(""))
            val pathIdx = result.indexOf(REDIRECT_PREFIX) + REDIRECT_PREFIX.count()
            response.headers.put(HttpHeaderNames.LOCATION, AsciiString(result.substring(pathIdx)))
        } else {
            val responseBody = responseResolverProvider.createResponseBody(result!!, args, handler.contentType)
            response.modify(handler.status.value, handler.contentType, toByteBuf(responseBody))
        }

    }

    private fun handleException(contentType: MimeType, exception: Throwable, request: WrappedHttpRequest, response: HttpResponse) {
        val exceptionHandler = exceptionHandlerProvider.getExceptionHandler(exception::class, contentType)
        if (exceptionHandler != null) {
            val args = createHandlerArguments(exceptionHandler, request, exception)
            val result = exceptionHandler.execute(*args)
            val responseBody = responseResolverProvider.createResponseBody(result!!, args, exceptionHandler.contentType)
            response.modify(exceptionHandler.status.value, exceptionHandler.contentType, toByteBuf(responseBody))
        } else {
            response.modify(INTERNAL_SERVER_ERROR, TEXT_PLAIN, toByteBuf(exception.message ?: ""))
        }
    }

    private fun createHandlerArguments(handler: HttpHandlerMetaInfo, request: WrappedHttpRequest, exception: Throwable? = null): Array<Any?> {
        val result = handler.parameters.map {
            when {
                it.annotation != null -> handlerParameterProvider.createParameterValue(it, request, handler)
                else -> defineContextParameter(it.clazz, request, exception)
            }
        }
        return result.toTypedArray()
    }

    private fun defineContextParameter(parameterType: Class<*>, request: WrappedHttpRequest, exception: Throwable? = null): Any {
        return when {
            FullHttpRequest::class.java == parameterType -> request
            Session::class.java == parameterType -> sessionStorage.findSession(request.originalRequest) ?: throw IllegalStateException("Session not found")
            Throwable::class.java.isAssignableFrom(parameterType) -> exception ?: throw IllegalStateException("Unknown exception specified")
            else -> DeserializationUtils.createBeanFromQueryString(parameterType, request.queryParams)
        }
    }

    private fun toByteBuf(message: String): ByteBuf = wrappedBuffer(message.toByteArray())

    private fun toByteBuf(message: ByteArray): ByteBuf = wrappedBuffer(message)

}