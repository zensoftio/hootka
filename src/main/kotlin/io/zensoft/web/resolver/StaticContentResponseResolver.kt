package io.zensoft.web.resolver

import io.zensoft.web.support.MimeType

class StaticContentResponseResolver: ResponseResolver {

    override fun supportsContentType(contentType: MimeType): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resolveResponseBody(result: Any, handlerArgs: Array<Any?>): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}