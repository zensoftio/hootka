package io.zensoft.web.support

import io.netty.handler.codec.http.FullHttpRequest

interface RequestFilter {

    fun doFilter(request: FullHttpRequest, response: HttpResponse)

}