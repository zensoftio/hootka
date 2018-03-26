package io.zensoft.web.default

import io.zensoft.web.annotation.AllowancePrecondition
import io.zensoft.web.annotation.AllowancePreconditions

@AllowancePreconditions(name = "roles")
class DefaultAllowancePreconditions {

    @AllowancePrecondition
    fun hasRole(role: String): Boolean {
        return "ADMIN" == role
    }

    @AllowancePrecondition
    fun hasAnyRole(roles: Set<String>): Boolean {
        return true
    }

}