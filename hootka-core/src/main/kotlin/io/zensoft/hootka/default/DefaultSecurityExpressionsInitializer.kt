package io.zensoft.hootka.default

import io.zensoft.hootka.api.SecurityExpressionInitializer
import io.zensoft.hootka.api.UserDetails
import io.zensoft.hootka.api.internal.security.RootSecurityExpressions

class DefaultSecurityExpressionsInitializer: SecurityExpressionInitializer {

    override fun createSecurityExpressions(principal: UserDetails?): RootSecurityExpressions {
        return RootSecurityExpressions(principal)
    }

}