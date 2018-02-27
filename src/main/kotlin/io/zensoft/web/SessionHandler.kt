package io.zensoft.web

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.cookie.ServerCookieEncoder
import io.zensoft.web.support.SessionStorage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@ChannelHandler.Sharable
@Component
class SessionHandler(
    private val sessionStorage: SessionStorage,
    @Value("\${server.session.cookie.name:session_id}") val sessionCookieName: String
) : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val request = msg as FullHttpRequest
        val cookies = sessionStorage.getCookies(request)
        val sessionCookie = cookies.find { it.name() == sessionCookieName }
        if (sessionCookie == null || sessionStorage.findSession(sessionCookie.value()) == null) {
            cookies.remove(sessionCookie)
            val session = sessionStorage.createSession()
            val newSessionCookie = sessionStorage.createSessionCookie(session)
            cookies.add(newSessionCookie)
            request.headers().set(HttpHeaderNames.COOKIE, ServerCookieEncoder.STRICT.encode(cookies))
        }
        ctx.fireChannelRead(request)
    }

}