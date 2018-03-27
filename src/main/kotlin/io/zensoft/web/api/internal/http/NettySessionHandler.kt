package io.zensoft.web.api.internal.http

import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.web.api.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class NettySessionHandler(
    @Value("\${server.session.cookie.name:session_id}") private val sessionCookieName: String,
    private val sessionStorage: SessionStorage
): SessionHandler<FullHttpRequest> {

    override fun getOrCreateSession(request: WrappedHttpRequest<FullHttpRequest>, response: WrappedHttpResponse): HttpSession {
        val cookies = request.getCookies()
        val sessionId = cookies[sessionCookieName]
        var session = sessionId
            ?.let { sessionStorage.findSession(sessionId) }
        if (session == null) {
            session = sessionStorage.createAndAssignSession(response)
        }
        return session
    }

}