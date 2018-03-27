package io.zensoft.web.api.internal.http

import io.zensoft.web.api.model.HttpStatus
import io.zensoft.web.api.model.MimeType
import io.zensoft.web.api.WrappedHttpResponse
import java.io.ByteArrayInputStream
import java.io.InputStream

class NettyWrappedHttpResponse(
    private var httpStatus: HttpStatus = HttpStatus.OK,
    private var contentType: MimeType = MimeType.APPLICATION_JSON,
    private var content: ByteArray? = null,
    private val headers: MutableMap<String, String> = HashMap(),
    private val cookies: MutableMap<String, String> = HashMap()
): WrappedHttpResponse {

    override fun getContent(): ByteArray? {
        return content
    }

    override fun getHttpStatus(): HttpStatus {
        return httpStatus
    }

    override fun getContentStream(): InputStream {
        return ByteArrayInputStream(content)
    }

    override fun getHeaders(): Map<String, String> {
        return headers
    }

    override fun getContentType(): MimeType {
        return contentType
    }

    override fun setHeader(key: String, value: String) {
        headers[key] = value
    }

    override fun setCookie(key: String, value: String) {
        cookies[key] = value
    }

    override fun mutate(status: HttpStatus, contentType: MimeType, content: ByteArray?) {
        this.httpStatus = status
        this.contentType = contentType
        this.content = content
    }

}