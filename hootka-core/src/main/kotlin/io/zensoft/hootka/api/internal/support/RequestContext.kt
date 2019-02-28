package io.zensoft.hootka.api.internal.support

import io.zensoft.hootka.api.HttpSession
import io.zensoft.hootka.api.WrappedHttpRequest
import io.zensoft.hootka.api.WrappedHttpResponse

class RequestContext(
    val request: WrappedHttpRequest,
    val response: WrappedHttpResponse,
    var session: HttpSession? = null
)