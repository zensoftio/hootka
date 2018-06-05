package io.zensoft.web.api.internal.server

import io.netty.buffer.Unpooled
import io.netty.buffer.Unpooled.*
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.util.AsciiString
import io.zensoft.web.api.*
import io.zensoft.web.api.exceptions.HandlerMethodNotFoundException
import io.zensoft.web.api.exceptions.PreconditionNotSatisfiedException
import io.zensoft.web.api.internal.http.NettyWrappedHttpRequest
import io.zensoft.web.api.internal.http.NettyWrappedHttpResponse
import io.zensoft.web.api.internal.provider.ExceptionHandlerProvider
import io.zensoft.web.api.internal.provider.HandlerParameterMapperProvider
import io.zensoft.web.api.internal.provider.MethodHandlerProvider
import io.zensoft.web.api.internal.provider.ResponseResolverProvider
import io.zensoft.web.api.internal.support.HttpHandlerMetaInfo
import io.zensoft.web.api.internal.security.AllowancePreconditionsHolder
import io.zensoft.web.api.internal.utils.DeserializationUtils
import io.zensoft.web.api.model.HttpStatus
import io.zensoft.web.api.model.MimeType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.reflect.InvocationTargetException

@ChannelHandler.Sharable
@Component
class HttpControllerHandler(
    private val pathHandlerProvider: MethodHandlerProvider,
    private val exceptionHandlerProvider: ExceptionHandlerProvider,
    private val sessionStorage: SessionStorage,
    private val sessionHandler: SessionHandler<FullHttpRequest>,
    private val handlerParameterProvider: HandlerParameterMapperProvider,
    private val responseResolverProvider: ResponseResolverProvider,
    private val preconditionsProvider: AllowancePreconditionsHolder
) : SimpleChannelInboundHandler<FullHttpRequest>() {

    companion object {
        private const val REDIRECT_PREFIX = "redirect:"

        private val log = LoggerFactory.getLogger(this::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        val response = NettyWrappedHttpResponse()
        val wrappedRequest = NettyWrappedHttpRequest.create(request)
        var handler: HttpHandlerMetaInfo? = null
        try {
            handler = pathHandlerProvider.getMethodHandler(wrappedRequest.getPath(), wrappedRequest.getMethod())
            handler.preconditionExpression?.let { preconditionsProvider.checkAllowance(it, wrappedRequest) }
            handleRequest(handler, wrappedRequest, response)
        } catch (ex: InvocationTargetException) {
            handleException(handler!!.contentType, ex.targetException!!, wrappedRequest, response)
        } catch (ex: HandlerMethodNotFoundException) {
            handleException(MimeType.TEXT_HTML, ex, wrappedRequest, response)
        } catch (ex: PreconditionNotSatisfiedException) {
            handleException(MimeType.TEXT_HTML, ex, wrappedRequest, response)
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
        writeResponse(ctx, null, NettyWrappedHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR,
            MimeType.TEXT_PLAIN, (cause.message ?: "").toByteArray()))
    }

    private fun writeResponse(ctx: ChannelHandlerContext, request: FullHttpRequest?, response: WrappedHttpResponse) {
        val result = if (response.getContent() != null) {
            val buf = Unpooled.wrappedBuffer(response.getContent())
            DefaultFullHttpResponse(HttpVersion.HTTP_1_1, response.getHttpStatus().value, buf)
        } else {
            DefaultFullHttpResponse(HttpVersion.HTTP_1_1, response.getHttpStatus().value)
        }
        for (header in response.getHeaders()) {
            result.headers().set(header.key, header.value)
        }
        if (result.content() != EMPTY_BUFFER) {
            result.headers().set(HttpHeaderNames.CONTENT_TYPE, response.getContentType().value.toString() + "; charset=UTF-8")
        }
        result.headers().set(HttpHeaderNames.CONTENT_LENGTH, result.content()!!.readableBytes())
        HttpUtil.setKeepAlive(result, HttpUtil.isKeepAlive(request))

        ctx.writeAndFlush(result)
    }

    private fun handleRequest(handler: HttpHandlerMetaInfo, request: WrappedHttpRequest<FullHttpRequest>, response: WrappedHttpResponse) {
        if (!handler.stateless) sessionHandler.getOrCreateSession(request, response)
        val args = createHandlerArguments(handler, request)
        val result = handler.execute(*args)
        if(result is String && result.startsWith(REDIRECT_PREFIX)) {
            response.mutate(HttpStatus.FOUND, handler.contentType)
            val pathIdx = result.indexOf(REDIRECT_PREFIX) + REDIRECT_PREFIX.count()
            response.setHeader(HttpHeaderNames.LOCATION.toString(), AsciiString(result.substring(pathIdx)).toString())
        } else {
            val responseBody = responseResolverProvider.createResponseBody(result!!, args, handler.contentType)
            response.mutate(handler.status, handler.contentType, responseBody)
        }

    }

    private fun handleException(contentType: MimeType, exception: Throwable, request: WrappedHttpRequest<FullHttpRequest>, response: WrappedHttpResponse) {
        val exceptionHandler = exceptionHandlerProvider.getExceptionHandler(exception::class, contentType)
        if (exceptionHandler != null) {
            val args = createHandlerArguments(exceptionHandler, request, exception)
            val result = exceptionHandler.execute(*args)
            val responseBody = responseResolverProvider.createResponseBody(result!!, args, exceptionHandler.contentType)
            response.mutate(exceptionHandler.status, exceptionHandler.contentType, responseBody)
        } else {
            response.mutate(HttpStatus.INTERNAL_SERVER_ERROR, MimeType.TEXT_PLAIN, (exception.message ?: "").toByteArray())
        }
    }

    private fun createHandlerArguments(handler: HttpHandlerMetaInfo, request: WrappedHttpRequest<FullHttpRequest>, exception: Throwable? = null): Array<Any?> {
        val result = handler.parameters.map {
            when {
                it.annotation != null -> handlerParameterProvider.createParameterValue(it, request, handler)
                else -> defineContextParameter(it.clazz, request, exception)
            }
        }
        return result.toTypedArray()
    }

    private fun defineContextParameter(parameterType: Class<*>, request: WrappedHttpRequest<FullHttpRequest>, exception: Throwable? = null): Any {
        return when {
            WrappedHttpRequest::class.java == parameterType -> request
            HttpSession::class.java == parameterType -> sessionStorage.resolveSession(request) ?: throw IllegalStateException("Session not found")
            Throwable::class.java.isAssignableFrom(parameterType) -> exception ?: throw IllegalStateException("Unknown exception specified")
            else -> DeserializationUtils.createBeanFromQueryString(parameterType, request.getQueryParameters())
        }
    }

}