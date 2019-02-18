package io.zensoft.web.api

import io.zensoft.web.api.model.HttpMethod
import java.io.InputStream
import java.nio.charset.Charset

interface WrappedHttpRequest {

    fun getPath(): String

    fun getMethod(): HttpMethod

    fun getQueryParameters(): Map<String, List<String>>

    fun getContentStream(): InputStream

    fun getContentAsString(charset: Charset): String

    fun getHeader(key: String): String?

    fun getCookies(): Map<String, String>

    fun getWrappedRequest(): Any

    fun getReferer(): String?

    fun getRemoteAddress(): String

}