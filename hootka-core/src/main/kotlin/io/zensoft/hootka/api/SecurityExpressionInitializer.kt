package io.zensoft.hootka.api

import io.zensoft.hootka.api.internal.security.RootSecurityExpressions

interface SecurityExpressionInitializer {

    fun createSecurityExpressions(principal: UserDetails?): RootSecurityExpressions

}