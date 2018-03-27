package io.zensoft.web.api

import io.zensoft.web.api.model.HttpStatus
import io.zensoft.web.api.model.MimeType
import java.io.InputStream

interface WrappedHttpResponse {

    fun getHttpStatus(): HttpStatus

    fun getContentStream(): InputStream

    fun getContent(): ByteArray?

    fun getHeaders(): Map<String, String>

    fun getContentType(): MimeType

    fun setHeader(key: String, value: String)

    fun setCookie(key: String, value: String)

    fun mutate(status: HttpStatus, contentType: MimeType, content: ByteArray? = null)

}