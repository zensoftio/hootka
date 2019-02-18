package io.zensoft.web.default

import io.zensoft.web.api.SecurityExpressionInitializer
import io.zensoft.web.api.UserDetails
import io.zensoft.web.api.internal.security.RootSecurityExpressions

class DefaultSecurityExpressionsInitializer: SecurityExpressionInitializer {

    override fun createSecurityExpressions(principal: UserDetails?): RootSecurityExpressions {
        return RootSecurityExpressions(principal)
    }

}