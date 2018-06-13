package io.zensoft.web.api.internal.http

import io.zensoft.web.api.*
import io.zensoft.web.api.properties.WebConfig
import org.springframework.stereotype.Component

@Component
class DefaultSessionHandler(
    private val webConfig: WebConfig,
    private val sessionStorage: SessionStorage
) : SessionHandler {

    override fun getOrCreateSession(request: WrappedHttpRequest, response: WrappedHttpResponse): HttpSession {
        val cookies = request.getCookies()
        val sessionId = cookies[webConfig.session.cookieName]
        var session = sessionId
            ?.let { sessionStorage.findSession(sessionId) }
        if (session == null) {
            session = sessionStorage.createAndAssignSession(response)
        }
        return session
    }

}