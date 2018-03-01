package io.zensoft.web.handler

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.cookie.ServerCookieEncoder
import io.netty.util.AsciiString
import io.zensoft.web.support.HttpResponse
import io.zensoft.web.support.SessionStorage
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
            val newSessionCookie = sessionStorage.createSessionCookie(session)
            cookies.add(newSessionCookie)
            request.headers().set(HttpHeaderNames.COOKIE, ServerCookieEncoder.STRICT.encode(cookies))
            response.headers.put(HttpHeaderNames.SET_COOKIE, AsciiString(ServerCookieEncoder.STRICT.encode(newSessionCookie)))
        }
    }

}