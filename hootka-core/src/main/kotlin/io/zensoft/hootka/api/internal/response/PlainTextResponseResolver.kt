package io.zensoft.hootka.api.internal.response

import io.zensoft.hootka.api.HttpResponseResolver
import io.zensoft.hootka.api.WrappedHttpResponse
import io.zensoft.hootka.api.model.MimeType

class PlainTextResponseResolver: HttpResponseResolver {

    override fun getContentType(): MimeType = MimeType.TEXT_PLAIN

    override fun resolveResponseBody(result: Any, handlerArgs: Array<Any?>, response: WrappedHttpResponse): ByteArray {
        if (result !is String) throw IllegalArgumentException("String return type should be for text/plain methods")
        return result.toString().toByteArray()
    }

}