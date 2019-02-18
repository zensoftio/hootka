package io.zensoft.web.api

interface SessionHandler {

    fun getOrCreateSession(request: WrappedHttpRequest, response: WrappedHttpResponse): HttpSession

}