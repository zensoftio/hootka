package io.zensoft.web.api

import io.zensoft.web.api.model.MimeType

interface HttpResponseResolver {

    fun supportsContentType(contentType: MimeType): Boolean

    fun resolveResponseBody(result: Any, handlerArgs: Array<Any?>, response: WrappedHttpResponse): ByteArray

}