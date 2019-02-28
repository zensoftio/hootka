package io.zensoft.hootka.api

import io.zensoft.hootka.api.internal.support.RequestContext

interface SecurityProvider<in T> {

    fun authenticate(authenticationDetails: T): UserDetails

    fun findPrincipal(context: RequestContext): UserDetails?

    fun encodePassword(plainPassword: String): String

    fun invalidate(request: WrappedHttpRequest, response: WrappedHttpResponse? = null)

    fun rememberMe(userDetails: UserDetails, response: WrappedHttpResponse)

    fun remindMe(requestContext: RequestContext): UserDetails?

}