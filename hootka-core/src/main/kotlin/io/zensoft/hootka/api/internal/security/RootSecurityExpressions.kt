package io.zensoft.hootka.api.internal.security

import io.zensoft.hootka.api.UserDetails

open class RootSecurityExpressions(
    private val principal: UserDetails? = null
) {

    fun hasRole(targetRole: String): Boolean {
        return null != principal && principal.getAuthorities().map { it.getAuthority() }.contains(targetRole)
    }

    fun hasAnyRole(roles: List<String>): Boolean {
        return null != principal && null != principal.getAuthorities().map { it.getAuthority() }.find { it in roles }
    }

    fun isAuthenticated(): Boolean {
        return null != principal
    }

    fun isAnonymous(): Boolean {
        return null == principal
    }

    fun anyone(): Boolean = true

}