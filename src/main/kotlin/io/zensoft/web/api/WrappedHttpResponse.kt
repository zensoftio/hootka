package io.zensoft.web.api

import io.netty.handler.codec.http.cookie.Cookie
import io.zensoft.web.api.model.HttpStatus
import io.zensoft.web.api.model.MimeType
import java.io.InputStream

interface WrappedHttpResponse {

    fun getHttpStatus(): HttpStatus

    fun getContentStream(): InputStream

    fun getContent(): ByteArray?

    fun getHeader(name: String): String?

    fun getHeaders(): Map<String, List<String>>

    fun getContentType(): MimeType

    fun setHeader(key: String, value: String)

    fun setCookie(key: String, value: String, httpOnly: Boolean, maxAge: Long?)

    fun getCookies(): List<Cookie>

    fun mutate(status: HttpStatus, contentType: MimeType, content: ByteArray? = null)

}