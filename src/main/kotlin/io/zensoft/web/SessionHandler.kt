package io.zensoft.web

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
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
import io.zensoft.web.support.Session
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@ChannelHandler.Sharable
@Component
class SessionHandler(
    @Value("\${server.session.cookie.name:session_id}") private val sessionCookieName: String,
    @Value("\${server.session.cookie.max-age}") private val sessionTimeout: Long
) : ChannelInboundHandlerAdapter() {

    private lateinit var sessionPool: Cache<String, Session>

    private val sessionAttribute = AttributeKey.newInstance<Cookie>("session_id")

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val request = msg as FullHttpRequest
        val cookies = getCookies(request)
        var sessionCookie = cookies.find { it.name() == sessionCookieName }
        if (sessionCookie == null || sessionPool.getIfPresent(sessionCookie.value()) == null) {
            sessionCookie = createSessionCookie()
            cookies.add(sessionCookie)
            request.headers().set(HttpHeaderNames.COOKIE, ServerCookieEncoder.STRICT.encode(cookies))
            ctx.channel().attr(sessionAttribute).set(sessionCookie)
        }
        ctx.fireChannelRead(request)
    }

    fun findSession(request: FullHttpRequest): Session? {
        val sessionCookie = getCookies(request).find { it.name() == sessionCookieName }
        return sessionCookie?.let { sessionPool.getIfPresent(it.value()) }
    }

    private fun getCookies(request: FullHttpRequest): MutableSet<Cookie> {
        val value = request.headers().get(HttpHeaderNames.COOKIE)
        return if (value != null) {
            ServerCookieDecoder.STRICT.decode(value)
        } else {
            mutableSetOf()
        }
    }

    private fun createSessionCookie(): Cookie {
        val sessionId = UUID.randomUUID().toString()
        sessionPool.put(sessionId, Session(sessionId))
        val sessionCookie = DefaultCookie(sessionCookieName, sessionId)
        sessionCookie.isHttpOnly = true
        sessionCookie.setMaxAge(sessionTimeout)
        sessionCookie.setPath("/")
        return sessionCookie
    }

    @PostConstruct
    private fun init() {
        sessionPool = Caffeine.newBuilder()
            .expireAfterAccess(sessionTimeout, TimeUnit.SECONDS)
            .build<String, Session>()
    }

}