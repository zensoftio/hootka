package io.zensoft.web.support

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.DefaultCookie
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class SessionStorage(
    @Value("\${server.session.cookie.name:session_id}") val sessionCookieName: String,
    @Value("\${server.session.cookie.max-age}") val sessionTimeout: Long
) {

    private lateinit var sessionPool: Cache<String, Session>

    fun findSession(request: FullHttpRequest): Session? {
        val sessionCookie = getCookies(request).find { it.name() == sessionCookieName }
        return sessionCookie?.let { sessionPool.getIfPresent(it.value()) }
    }

    fun findSession(id: String): Session? = sessionPool.getIfPresent(id)

    fun createSession(): Session {
        val session = Session(UUID.randomUUID().toString())
        sessionPool.put(session.id, session)
        return session
    }

    fun createSessionCookie(session: Session): Cookie {
        val sessionCookie = DefaultCookie(sessionCookieName, session.id)
        sessionCookie.isHttpOnly = true
        sessionCookie.setMaxAge(sessionTimeout)
        sessionCookie.setPath("/")
        return sessionCookie
    }

    fun getCookies(request: FullHttpRequest): MutableSet<Cookie> {
        val value = request.headers().get(HttpHeaderNames.COOKIE)
        return if (value != null) {
            ServerCookieDecoder.STRICT.decode(value)
        } else {
            mutableSetOf()
        }
    }

    @PostConstruct
    private fun init() {
        sessionPool = Caffeine.newBuilder()
            .expireAfterAccess(sessionTimeout, TimeUnit.SECONDS)
            .build<String, Session>()
    }

}