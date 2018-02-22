package io.zensoft.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.buffer.Unpooled.wrappedBuffer
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON
import io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.ServerCookieEncoder
import io.netty.util.AttributeKey
import io.zensoft.utils.NumberUtils
import io.zensoft.web.support.HttpMethod
import io.zensoft.web.support.HttpResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import java.nio.charset.Charset

@ChannelHandler.Sharable
@Component
class HttpControllerHandler(
        private val pathHandlerProvider: PathHandlerProvider
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
        } catch (e: Exception) {
            e.printStackTrace()
            response.modify(HttpResponseStatus.INTERNAL_SERVER_ERROR, TEXT_PLAIN, e.message ?: "")
        }

        writeResponse(ctx, response)
    }

    private fun handleRequest(request: FullHttpRequest, response: HttpResponse) {
        var responseBody = ""
        val handler = pathHandlerProvider.getHandler(request)
        if (handler == null) {
            response.modify(HttpResponseStatus.NOT_FOUND, TEXT_PLAIN, "Not found")
            return
        } else if(handler.httpMethod != HttpMethod.valueOf(request.method().name())) {
            response.modify(HttpResponseStatus.NOT_FOUND, TEXT_PLAIN, "Http Method ${request.method().name()} is not supported")
            return
        }
        var args = if(handler.entityType != null) {
            arrayOf(jacksonObjectMapper.readValue(request.content().toString(charset), handler.entityType))
        } else {
            emptyArray()
        }
        val parsedPatterns = pathMatcher.extractUriTemplateVariables(handler.path, request.uri())
        handler.pathVariables.forEach {
            val value = NumberUtils.parseNumber(parsedPatterns[it.key]!!, it.value)
            args = args.plus(value)
        }

        val result = handler.execute(*args)

        if (result is String) {
            responseBody = result
        } else if (result != null) {
            responseBody = jacksonObjectMapper.writeValueAsString(result)
        }
        response.modify(HttpResponseStatus.OK, APPLICATION_JSON, responseBody)
    }

    private fun writeResponse(ctx: ChannelHandlerContext, response: HttpResponse) {
        val buf = wrappedBuffer((response.content as String).toByteArray())
        val result = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, response.status, buf)

        val channel = ctx.channel()
        val attribute = AttributeKey.valueOf<Cookie>("session_id")
        val sessionCookie = if (channel.hasAttr(attribute)) channel.attr(attribute).get() else null

        sessionCookie?.let { result.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(sessionCookie)) }
        result.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes())
        result.headers().set(HttpHeaderNames.CONTENT_TYPE, response.mimeType.toString() + "; charset=UTF-8")
        HttpUtil.setKeepAlive(result, true)

        ctx.writeAndFlush(result)
    }

    /**
     * Marked deprecated as method moved from {@link ChannelHandler} to it's child interface {@link ChannelInboundHandler}
     */
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Something went wrong", cause)
        writeResponse(ctx, HttpResponse(TEXT_PLAIN, HttpResponseStatus.INTERNAL_SERVER_ERROR, null, cause.message ?: ""))
    }



}