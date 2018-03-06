package io.zensoft.web.support

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.QueryStringDecoder

class WrappedHttpRequest(
    val path: String,
    val method: HttpMethod,
    val queryParams: Map<String, List<String>>,
    val originalRequest: FullHttpRequest
) {
    companion object {

        fun wrap(request: FullHttpRequest): WrappedHttpRequest {
            val decoder = QueryStringDecoder(request.uri())
            val method = HttpMethod.valueOf(request.method().name())
            return WrappedHttpRequest(decoder.path(), method, decoder.parameters(), request)
        }

    }
}