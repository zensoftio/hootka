package io.zensoft.hootka.api

interface UserDetailsService {

    fun findUserDetailsByUsername(value: String): UserDetails?

}