package io.zensoft.web.api.model

import io.zensoft.web.api.WrappedHttpRequest
import io.zensoft.web.api.WrappedHttpResponse

class SimpleAuthenticationDetails(
    val username: String,
    val password: String,
    val request: WrappedHttpRequest,
    val response: WrappedHttpResponse,
    val rememberMe: Boolean = false
)