package io.zensoft.web.support

import io.netty.handler.codec.http.FullHttpRequest
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class RequestFilterChain(
        private val context: ApplicationContext
) {

    private lateinit var chain: List<RequestFilter>

    fun isRequestAcceptable(request: FullHttpRequest, response: HttpResponse): Boolean {
        for(filter in chain) {
            filter.doFilter(request, response)
            if(response.status != null) return false
        }
        return true
    }

    @PostConstruct
    private fun init() {
        chain = context.getBeansOfType(RequestFilter::class.java).values.toList()
    }

}