package io.zensoft.web.resolver

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.zensoft.web.support.MimeType
import org.springframework.stereotype.Component

@Component
class JsonResponseResolver: ResponseResolver {

    private val jsonMapper = jacksonObjectMapper()

    override fun supportsContentType(contentType: MimeType): Boolean = MimeType.APPLICATION_JSON == contentType

    override fun resolveResponseBody(result: Any, handlerArgs: Array<Any>): String {
        return jsonMapper.writeValueAsString(result)
    }

}