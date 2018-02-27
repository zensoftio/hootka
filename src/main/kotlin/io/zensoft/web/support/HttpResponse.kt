package io.zensoft.web.support

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.util.AsciiString

class HttpResponse(
        var mimeType: MimeType? = null,
        var status: HttpResponseStatus? = null,
        var content: ByteBuf? = null,
        var headers: MutableMap<AsciiString, AsciiString> = mutableMapOf()
) {
    fun modify(status: HttpResponseStatus, mimeType: MimeType, content: ByteBuf) {
        this.mimeType = mimeType
        this.status = status
        this.content = content
    }
}