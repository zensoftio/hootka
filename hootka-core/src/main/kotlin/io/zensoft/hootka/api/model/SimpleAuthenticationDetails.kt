package io.zensoft.hootka.api.model

import io.zensoft.hootka.api.WrappedHttpRequest
import io.zensoft.hootka.api.WrappedHttpResponse

class SimpleAuthenticationDetails(
    val username: String,
    val password: String,
    val request: WrappedHttpRequest,
    val response: WrappedHttpResponse,
    val rememberMe: Boolean = false
)