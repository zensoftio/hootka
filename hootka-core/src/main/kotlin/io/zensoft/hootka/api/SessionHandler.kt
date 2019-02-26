package io.zensoft.hootka.api

interface SessionHandler {

    fun getOrCreateSession(request: WrappedHttpRequest, response: WrappedHttpResponse): HttpSession

}