package io.zensoft.sample.domain

import io.zensoft.hootka.api.UserAuthority
import io.zensoft.hootka.api.UserDetails

class User(
    private val email: String,
    private var password: String,
    private val roles: Set<UserAuthority>,
    var enabled: Boolean
) : UserDetails {
    override fun getUsername(): String = email

    override fun getPassword(): String = password

    override fun getAuthorities(): Set<UserAuthority> = roles

    override fun isEnabled(): Boolean = enabled

}