package io.zensoft.web.api

import io.zensoft.web.api.internal.security.RootSecurityExpressions

interface SecurityExpressionInitializer {

    fun createSecurityExpressions(principal: UserDetails?): RootSecurityExpressions

}