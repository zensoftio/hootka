package io.zensoft.web.api.internal.response

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.zensoft.web.api.HttpResponseResolver
import io.zensoft.web.api.model.MimeType
import org.springframework.stereotype.Component

@Component
class JsonResponseResolver: HttpResponseResolver {

    private val jsonMapper = jacksonObjectMapper()

    override fun supportsContentType(contentType: MimeType): Boolean = MimeType.APPLICATION_JSON == contentType

    override fun resolveResponseBody(result: Any, handlerArgs: Array<Any?>): ByteArray {
        return jsonMapper.writeValueAsBytes(result)
    }

}