package io.zensoft.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled.wrappedBuffer
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.ServerCookieEncoder
import io.netty.util.AttributeKey
import io.zensoft.annotation.PathVariable
import io.zensoft.annotation.RequestBody
import io.zensoft.utils.NumberUtils
import io.zensoft.web.support.*
import io.zensoft.web.support.HttpMethod
import io.zensoft.web.support.HttpResponse
import io.zensoft.web.support.MimeType.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import java.lang.reflect.InvocationTargetException
import java.nio.charset.Charset

@ChannelHandler.Sharable
@Component
class HttpControllerHandler(
    private val pathHandlerProvider: PathHandlerProvider,
    private val sessionHandler: SessionHandler
) : SimpleChannelInboundHandler<FullHttpRequest>() {

    private val pathMatcher = AntPathMatcher()

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
        private val jacksonObjectMapper = jacksonObjectMapper()
        private val charset = Charset.forName("UTF-8")
    }

    override fun channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        val response = HttpResponse()
        try {
            handleRequest(request, response)
        } catch (ex: InvocationTargetException) {
            handleException(ex.targetException!!, request, response)
        }

        writeResponse(ctx, response)
    }

    private fun writeResponse(ctx: ChannelHandlerContext, response: HttpResponse) {
        val buf = response.content
        val result = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, response.status, buf)

        val channel = ctx.channel()
        val attribute = AttributeKey.valueOf<Cookie>("session_id")
        val createdSession = channel.attr(attribute).getAndSet(null)
        createdSession?.let { result.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(it)) }

        result.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf!!.readableBytes())
        result.headers().set(HttpHeaderNames.CONTENT_TYPE, response.mimeType!!.value.toString() + "; charset=UTF-8")
        HttpUtil.setKeepAlive(result, true)

        ctx.writeAndFlush(result)
    }

    private fun handleRequest(request: FullHttpRequest, response: HttpResponse) {
        var responseBody = ""
        val handler = pathHandlerProvider.getMethodHandler(request)
        if (handler == null) {
            response.modify(NOT_FOUND, TEXT_PLAIN, wrappedBuffer("Not found".toByteArray()))
            return
        }
        if (handler.httpMethod != HttpMethod.valueOf(request.method().name())) {
            response.modify(METHOD_NOT_ALLOWED, TEXT_PLAIN, toByteBuf("Http Method ${request.method()} is not supported"))
            return
        }

        val args = createHandlerArguments(handler, request)
        val result = handler.execute(*args)
        if (result is String) {
            responseBody = result
        } else if (result != null) {
            responseBody = jacksonObjectMapper.writeValueAsString(result)
        }
        response.modify(handler.status.value, handler.contentType, toByteBuf(responseBody))
    }

    private fun handleException(exception: Throwable, request: FullHttpRequest, response: HttpResponse) {
        var responseBody = ""
        val handler = pathHandlerProvider.getExceptionHandler(exception::class)
        if (handler != null) {
            val args = createHandlerArguments(handler, request, exception)
            val result = handler.execute(*args)
            if (result is String) {
                responseBody = result
            } else if (result != null) {
                responseBody = jacksonObjectMapper.writeValueAsString(result)
            }
            response.modify(handler.status.value, handler.contentType, toByteBuf(responseBody))
        } else {
            response.modify(INTERNAL_SERVER_ERROR, TEXT_PLAIN, toByteBuf(exception.message ?: ""))
        }
    }

    private fun createHandlerArguments(handler: HttpHandlerMetaInfo, request: FullHttpRequest, exception: Throwable? = null): Array<Any> {
        val pathVariables = if (handler.path.isNotEmpty()) {
            pathMatcher.extractUriTemplateVariables(handler.path, request.uri())
        } else {
            emptyMap()
        }
        return handler.parameters.map {
            when (it.value.annotation) {
                is RequestBody -> jacksonObjectMapper.readValue(request.content().toString(charset), it.value.clazz)
                is PathVariable -> NumberUtils.parseNumber(pathVariables[it.key]!!, it.value.clazz)
                else -> defineContextParameter(it.value.clazz, request, exception)
            }
        }.toTypedArray()
    }

    private fun defineContextParameter(parameterType: Class<*>, request: FullHttpRequest, exception: Throwable? = null): Any {
        return when {
            parameterType == FullHttpRequest::class.java -> request
            Throwable::class.java.isAssignableFrom(parameterType) -> exception ?:
                throw IllegalStateException("Unknown exception specified")
            parameterType == Session::class.java -> sessionHandler.findSession(request) ?:
                throw IllegalStateException("Session not found")
            else -> throw IllegalArgumentException("Unknown context parameter with type $parameterType")
        }
    }

    /**
     * Marked deprecated as method moved from [io.netty.channel.ChannelHandler] to it's child interface [io.netty.channel.ChannelInboundHandler]
     */
    @Throws(Exception::class)
    @Suppress("OverridingDeprecatedMember")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Something went wrong", cause)
        writeResponse(ctx, HttpResponse(TEXT_PLAIN, INTERNAL_SERVER_ERROR, null, toByteBuf(cause.message ?: "")))
    }

    private fun toByteBuf(message: String): ByteBuf = wrappedBuffer(message.toByteArray())

}