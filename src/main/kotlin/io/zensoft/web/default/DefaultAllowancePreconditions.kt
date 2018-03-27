package io.zensoft.web.default

import io.zensoft.web.annotation.AllowancePreconditions
import io.zensoft.web.support.Session

@AllowancePreconditions(name = "roles")
class DefaultAllowancePreconditions {

    fun hasRole(targetRole: String, session: Session): Boolean {
        val userRole = extractRole(session)
        return userRole == targetRole
    }

    fun hasAnyRole(roles: Set<String>, session: Session): Boolean {
        val userRole = extractRole(session)
        return roles.contains(userRole)
    }

    private fun extractRole(session: Session): String {
        return session.getAttribute("user_role") as String
    }

}