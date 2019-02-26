package io.zensoft.hootka.api

interface UserDetails {

    fun getUsername(): String

    fun getPassword(): String

    fun getAuthorities(): Set<UserAuthority>

    fun isEnabled(): Boolean

}