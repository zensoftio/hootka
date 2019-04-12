package io.zensoft.hootka.api

import io.zensoft.hootka.api.model.MimeType

interface HttpResponseResolver {

    fun getContentType(): MimeType

    fun resolveResponseBody(result: Any, handlerArgs: Array<Any?>, response: WrappedHttpResponse): ByteArray

}