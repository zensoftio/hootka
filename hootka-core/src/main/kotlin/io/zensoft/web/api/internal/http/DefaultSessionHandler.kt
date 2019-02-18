package io.zensoft.web.api.internal.http

import io.zensoft.web.api.*
import io.zensoft.web.api.properties.WebConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DefaultSessionHandler(
    private val sessionStorage: SessionStorage,
    private val cookieName: String
) : SessionHandler {

    override fun getOrCreateSession(request: WrappedHttpRequest, response: WrappedHttpResponse): HttpSession {
        val cookies = request.getCookies()
        val sessionId = cookies[cookieName]
        var session = sessionId
            ?.let { sessionStorage.findSession(sessionId) }
        if (session == null) {
            session = sessionStorage.createAndAssignSession(response)
        }
        return session
    }

}