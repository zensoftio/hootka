package io.zensoft.web

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.DefaultCookie
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.util.AttributeKey
import io.zensoft.web.session.SessionPool
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@ChannelHandler.Sharable
@Component
class SessionHandler(
        private @Value("\${server.session.cookie.name:session_id}") val sessionCookieName: String,
        private @Value("\${server.session.cookie.max-age}") val sessionTimeout: Long,
        private val sessionPool: SessionPool
): ChannelInboundHandlerAdapter() {

    private val sessionAttribute = AttributeKey.newInstance<Cookie>("session_id")

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val request = msg as FullHttpRequest
        val cookies = getCookies(request)
        var sessionCookie = cookies?.get(sessionCookieName)?.first() //TODO
        if(sessionCookie == null || sessionPool.getSession(sessionCookie.value()) == null) {
            val sessionId = UUID.randomUUID().toString()
            sessionPool.createSession(sessionId)
            sessionCookie = DefaultCookie(sessionCookieName, sessionId)
            sessionCookie.isHttpOnly = true
            sessionCookie.setMaxAge(sessionTimeout)
            ctx.channel().attr(sessionAttribute).set(sessionCookie)
        }
        ctx.fireChannelRead(request)
    }

    private fun getCookies(request: FullHttpRequest): Map<String, List<Cookie>>? {
        val value = request.headers().get(HttpHeaderNames.COOKIE)
        return value?.let { ServerCookieDecoder.STRICT.decode(value).groupBy { it.name() } } //TODO
    }

}