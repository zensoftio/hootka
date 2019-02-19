package io.zensoft.web.api.internal.provider

import io.zensoft.web.api.HttpResponseResolver
import io.zensoft.web.api.WrappedHttpResponse
import io.zensoft.web.api.model.MimeType
import org.springframework.context.ApplicationContext
import javax.annotation.PostConstruct

class ResponseResolverProvider(
    private val applicationContext: ApplicationContext
) {

    private lateinit var responseResolvers: List<HttpResponseResolver>

    fun createResponseBody(result: Any, handlerArgs: Array<Any?>, mimeType: MimeType, response: WrappedHttpResponse): ByteArray {
        if (result === Unit) return ByteArray(0)
        responseResolvers
            .filter { it.supportsContentType(mimeType) }
            .forEach { return it.resolveResponseBody(result, handlerArgs, response) }
        throw IllegalArgumentException("Unsupported response content type $mimeType")
    }

    @PostConstruct
    private fun init() {
        responseResolvers = applicationContext.getBeansOfType(HttpResponseResolver::class.java).values.toList()
    }

}