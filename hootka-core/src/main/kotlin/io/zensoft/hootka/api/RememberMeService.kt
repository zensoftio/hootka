package io.zensoft.hootka.api

import io.zensoft.hootka.api.internal.support.RequestContext

interface RememberMeService {

    fun createToken(userDetails: UserDetails, response: WrappedHttpResponse): String

    fun invalidateToken(request: WrappedHttpRequest, response: WrappedHttpResponse)

    fun performAutoAuthentication(requestContext: RequestContext): UserDetails?

}