package io.zensoft.web.api.internal.http

import io.netty.buffer.ByteBufInputStream
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.zensoft.web.api.model.HttpMethod
import io.zensoft.web.api.WrappedHttpRequest
import java.io.InputStream
import java.nio.charset.Charset

class NettyWrappedHttpRequest(
    private val wrappedRequest: FullHttpRequest,
    private val path: String,
    private val method: HttpMethod,
    private val uriQueryParameters: Map<String, List<String>>,
    private var headers: Map<String, String>? = null,
    private var cookies: Map<String, String>? = null
): WrappedHttpRequest<FullHttpRequest> {

    companion object {

        fun create(request: FullHttpRequest): NettyWrappedHttpRequest {
            val pathDecoder = QueryStringDecoder(request.uri())
            return NettyWrappedHttpRequest(request,
                pathDecoder.path(),
                HttpMethod.valueOf(request.method().name()),
                pathDecoder.parameters())
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

    override fun getHeaders(): Map<String, String> {
        if (headers == null) {
            headers = wrappedRequest.headers().associate { it.key to it.value }
        }
        return headers!!
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
        return getHeaders()[HttpHeaderNames.REFERER.toString()]
    }

}