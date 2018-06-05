package io.zensoft.web.api

interface SessionHandler<in T> {

    fun getOrCreateSession(request: WrappedHttpRequest<T>, response: WrappedHttpResponse): HttpSession

}