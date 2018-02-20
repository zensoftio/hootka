package io.zensoft.web.support

import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.util.AsciiString

class HttpResponse(
        var mimeType: AsciiString? = null,
        var status: HttpResponseStatus? = null,
        var headers: MutableMap<AsciiString, AsciiString>? = mutableMapOf(),
        var content: Any? = null
) {
    fun modify(status: HttpResponseStatus, mimeType: AsciiString, content: Any) {
        this.mimeType = mimeType
        this.status = status
        this.content = content
    }
}