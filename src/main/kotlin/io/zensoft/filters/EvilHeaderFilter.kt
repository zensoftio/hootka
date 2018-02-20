package io.zensoft.filters

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpResponseStatus
import io.zensoft.web.support.HttpResponse
import io.zensoft.web.support.RequestFilter
import org.springframework.stereotype.Component

@Component
class EvilHeaderFilter: RequestFilter {

    override fun doFilter(request: FullHttpRequest, response: HttpResponse) {
        if(request.headers().contains("evilHeader")) {
            response.modify(HttpResponseStatus.NOT_ACCEPTABLE, HttpHeaderValues.TEXT_PLAIN, "Not acceptable")
        }
    }

}