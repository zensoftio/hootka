package io.zensoft.web.resolver

import io.zensoft.web.support.MimeType

interface ResponseResolver {

    fun supportsContentType(contentType: MimeType): Boolean

    fun resolveResponseBody(result: Any, handlerArgs: Array<Any?>): ByteArray

}