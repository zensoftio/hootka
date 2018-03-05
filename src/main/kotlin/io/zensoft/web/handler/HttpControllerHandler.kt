package io.zensoft.web.handler

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled.wrappedBuffer
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpResponseStatus.*
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
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        val response = HttpResponse()
        val wrappedRequest = WrappedHttpRequest.wrapRequest(request)
        try {
            handleRequest(wrappedRequest, response)
        } catch (ex: InvocationTargetException) {
            handleException(ex.targetException!!, wrappedRequest, response)
        } catch (ex: Exception) {
            handleException(ex, wrappedRequest, response)
        }

        writeResponse(ctx, request, response)
    }

    private fun writeResponse(ctx: ChannelHandlerContext, request: FullHttpRequest?, response: HttpResponse) {
        val buf = response.content
        val result = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, response.status, buf)

        for (header in response.headers) {
            result.headers().set(header.key, header.value)
        }
        result.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf!!.readableBytes())
        result.headers().set(HttpHeaderNames.CONTENT_TYPE, response.mimeType!!.value.toString() + "; charset=UTF-8")
        HttpUtil.setKeepAlive(result, true)

        ctx.writeAndFlush(result)
    }

    private fun handleRequest(request: WrappedHttpRequest, response: HttpResponse) {
        val handler = pathHandlerProvider.getMethodHandler(request.path, request.method)
        if (!handler.stateless) sessionHandler.handleSession(request.originalRequest, response)
        val args = createHandlerArguments(handler, request)
        val result = handler.execute(*args)
        val responseBody = responseResolverProvider.createResponseBody(result!!, args, handler.contentType)
        response.modify(handler.status.value, handler.contentType, toByteBuf(responseBody))
    }

    private fun handleException(exception: Throwable, request: WrappedHttpRequest, response: HttpResponse) {
        val handler = exceptionHandlerProvider.getExceptionHandler(exception::class)
        if (handler != null) {
            val args = createHandlerArguments(handler, request, exception)
            val result = handler.execute(*args)
            val responseBody = responseResolverProvider.createResponseBody(result!!, args, handler.contentType)
            response.modify(handler.status.value, handler.contentType, toByteBuf(responseBody))
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

    /**
     * Marked deprecated as method moved from [io.netty.channel.ChannelHandler] to it's child interface [io.netty.channel.ChannelInboundHandler]
     */
    @Throws(Exception::class)
    @Suppress("OverridingDeprecatedMember")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Something went wrong", cause)
        writeResponse(ctx, null, HttpResponse(TEXT_PLAIN, INTERNAL_SERVER_ERROR, toByteBuf(cause.message ?: "")))
    }

    private fun toByteBuf(message: String): ByteBuf = wrappedBuffer(message.toByteArray())

}