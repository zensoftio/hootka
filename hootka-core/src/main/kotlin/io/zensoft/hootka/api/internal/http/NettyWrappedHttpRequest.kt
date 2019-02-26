package io.zensoft.hootka.api.internal.http

import io.netty.buffer.ByteBufInputStream
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.zensoft.hootka.api.WrappedHttpRequest
import io.zensoft.hootka.api.model.HttpMethod
import java.io.InputStream
import java.nio.charset.Charset

class NettyWrappedHttpRequest(
    private val wrappedRequest: FullHttpRequest,
    private val path: String,
    private val method: HttpMethod,
    private val uriQueryParameters: Map<String, List<String>>,
    private val remoteAddress: String,
    private var headers: Map<String, String>? = null,
    private var cookies: Map<String, String>? = null
) : WrappedHttpRequest {

    companion object {

        fun create(request: FullHttpRequest, remoteAddress: String): NettyWrappedHttpRequest {
            val pathDecoder = QueryStringDecoder(request.uri())
            return NettyWrappedHttpRequest(request,
                pathDecoder.path(),
                HttpMethod.valueOf(request.method().name()),
                pathDecoder.parameters(),
                remoteAddress)
        }

    }

    override fun getPath(): String {
        return path
    }

    override fun getMethod(): HttpMethod {
        return method
    }

    override fun getQueryParameters(): Map<String, List<String>> {
        return uriQueryParameters
    }

    override fun getContentStream(): InputStream {
        return ByteBufInputStream(wrappedRequest.content())
    }

    override fun getContentAsString(charset: Charset): String {
        return wrappedRequest.content().toString(charset)
    }

    override fun getHeader(key: String): String? {
        return wrappedRequest.headers().get(key)
    }

    override fun getCookies(): Map<String, String> {
        if (cookies == null) {
            val encodedCookies = wrappedRequest.headers().get(HttpHeaderNames.COOKIE)
            cookies = encodedCookies?.let {
                ServerCookieDecoder.STRICT.decode(encodedCookies).associate { it.name() to it.value() }
            } ?: HashMap()
        }
        return cookies!!
    }

    override fun getWrappedRequest(): FullHttpRequest {
        return wrappedRequest
    }

    override fun getReferer(): String? {
        return getHeader(HttpHeaderNames.REFERER.toString())
    }

    override fun getRemoteAddress(): String {
        return remoteAddress
    }

}