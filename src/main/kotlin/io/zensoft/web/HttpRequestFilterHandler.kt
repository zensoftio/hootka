package io.zensoft.web

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.DefaultCookie
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http.cookie.ServerCookieEncoder
import io.netty.util.AttributeKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@ChannelHandler.Sharable
@Component
class HttpRequestFilterHandler(
        private @Value("\${server.session.cookie.name:session_id}") val sessionCookieName: String,
        private @Value("\${server.session.cookie.max-age}") val sessionTimeout: Long
): ChannelInboundHandlerAdapter() {

    private val sessionAttribute = AttributeKey.newInstance<Cookie>("session_id")

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val request = msg as FullHttpRequest
        val cookies = getCookies(request)
        var sessionCookie = cookies?.get(sessionCookieName)?.first()
        if(sessionCookie == null) {
            sessionCookie = DefaultCookie(sessionCookieName, UUID.randomUUID().toString())
            sessionCookie.isHttpOnly = true
            sessionCookie.setMaxAge(sessionTimeout)
            request.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(sessionCookie))
            ctx.channel().attr(sessionAttribute).set(sessionCookie)
        }
        ctx.fireChannelRead(request)
    }

    private fun getCookies(request: FullHttpRequest): Map<String, List<Cookie>>? {
        val value = request.headers().get(HttpHeaderNames.COOKIE)
        return value?.let { ServerCookieDecoder.STRICT.decode(value).groupBy { it.name() } } //TODO
    }

}