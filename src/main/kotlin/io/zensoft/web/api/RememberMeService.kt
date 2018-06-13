package io.zensoft.web.api

import io.zensoft.web.api.internal.support.RequestContext

interface RememberMeService {

    fun createToken(userDetails: UserDetails, response: WrappedHttpResponse): String

    fun invalidateToken(request: WrappedHttpRequest, response: WrappedHttpResponse)

    fun performAutoAuthentication(requestContext: RequestContext): UserDetails?

}