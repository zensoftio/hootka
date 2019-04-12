package io.zensoft.hootka.api.model

import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.util.AsciiString

enum class MimeType(val value: AsciiString) {
    TEXT_PLAIN(HttpHeaderValues.TEXT_PLAIN),
    APPLICATION_JSON(HttpHeaderValues.APPLICATION_JSON),
    APPLICATION_OCTET_STREAM(HttpHeaderValues.APPLICATION_OCTET_STREAM),
    BYTES(HttpHeaderValues.BYTES),
    TEXT_HTML(AsciiString.cached("text/html")),
    TEXT_CSS(AsciiString.cached("text/css")),
    TEXT_JAVASCRIPT(AsciiString.cached("text/javascript")),
    IMAGE_GIF(AsciiString.cached("image/gif")),
    IMAGE_PNG(AsciiString.cached("image/png")),
    IMAGE_JPEG(AsciiString.cached("image/jpeg")),
    IMAGE_SVG(AsciiString.cached("image/svg+xml")),
    IMAGE_ICO(AsciiString.cached("image/x-icon")),
    FONT_TTF(AsciiString.cached("font/ttf")),
    FONT_WOFF2(AsciiString.cached("font/woff2"))
}