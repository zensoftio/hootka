package io.zensoft.web.api.internal.support

import io.zensoft.web.api.HttpSession
import io.zensoft.web.api.WrappedHttpRequest
import io.zensoft.web.api.WrappedHttpResponse

class RequestContext(
    val request: WrappedHttpRequest,
    val response: WrappedHttpResponse,
    var session: HttpSession? = null
)