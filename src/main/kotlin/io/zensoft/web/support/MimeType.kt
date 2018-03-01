package io.zensoft.web.support

import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.util.AsciiString

enum class MimeType(val value: AsciiString) {
    TEXT_PLAIN(HttpHeaderValues.TEXT_PLAIN),
    APPLICATION_JSON(HttpHeaderValues.APPLICATION_JSON),
    APPLICATION_OCTET_STREAM(HttpHeaderValues.APPLICATION_OCTET_STREAM),
    BYTES(HttpHeaderValues.BYTES),
    TEXT_HTML(AsciiString.cached("text/html"))
}