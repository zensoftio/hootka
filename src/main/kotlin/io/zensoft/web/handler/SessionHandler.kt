package io.zensoft.web.handler

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.cookie.ServerCookieEncoder
import io.netty.util.AsciiString
import io.zensoft.web.support.HttpResponse
import io.zensoft.web.support.Session
import io.zensoft.web.support.SessionStorage
import io.zensoft.web.support.WrappedHttpRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SessionHandler(
    private val sessionStorage: SessionStorage,
    @Value("\${server.session.cookie.name:session_id}") val sessionCookieName: String
) {

    fun handleSession(request: FullHttpRequest, response: HttpResponse) {
        val cookies = sessionStorage.getCookies(request)
        val sessionCookie = cookies.find { it.name() == sessionCookieName }
        if (sessionCookie == null || sessionStorage.findSession(sessionCookie.value()) == null) {
            cookies.remove(sessionCookie)
            val session = sessionStorage.createSession()
            session.setAttribute("user_role", "ADMIN")
            val newSessionCookie = sessionStorage.createSessionCookie(session)
            cookies.add(newSessionCookie)
            request.headers().set(HttpHeaderNames.COOKIE, ServerCookieEncoder.STRICT.encode(cookies))
            response.headers.put(HttpHeaderNames.SET_COOKIE, AsciiString(ServerCookieEncoder.STRICT.encode(newSessionCookie)))
        }
    }

    fun findSession(wrappedHttpRequest: WrappedHttpRequest): Session? {
        val cookies = sessionStorage.getCookies(wrappedHttpRequest.originalRequest)
        val sessionCookie = cookies.find { it.name() == sessionCookieName }
        return sessionCookie?.let { sessionStorage.findSession(it.value()) }
    }

}