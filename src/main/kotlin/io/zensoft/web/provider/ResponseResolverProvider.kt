package io.zensoft.web.provider

import io.zensoft.web.resolver.ResponseResolver
import io.zensoft.web.support.MimeType
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class ResponseResolverProvider(
    private val applicationContext: ApplicationContext
) {

    private lateinit var responseResolvers: List<ResponseResolver>

    fun createResponseBody(result: Any, handlerArgs: Array<Any?>, mimeType: MimeType): String {
        for (responseResolver in responseResolvers) {
            if (responseResolver.supportsContentType(mimeType)) {
                return responseResolver.resolveResponseBody(result, handlerArgs)
            }
        }
        throw IllegalArgumentException("Unsupported response content type $mimeType")
    }

    @PostConstruct
    private fun init() {
        responseResolvers = applicationContext.getBeansOfType(ResponseResolver::class.java).values.toList()
    }

}