package io.zensoft.web.default

import io.zensoft.web.annotation.AllowancePreconditions
import io.zensoft.web.api.HttpSession

@AllowancePreconditions(name = "roles")
class DefaultAllowancePreconditions {

    fun hasRole(targetRole: String, session: HttpSession): Boolean {
        val userRole = extractRole(session)
        return userRole == targetRole
    }

    fun hasAnyRole(roles: Set<String>, session: HttpSession): Boolean {
        val userRole = extractRole(session)
        return roles.contains(userRole)
    }

    private fun extractRole(session: HttpSession): String? {
        return session.findTypedAttribute("user_role", String::class.java)
    }

}