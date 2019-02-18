package io.zensoft.web.api

interface UserDetailsService {

    fun findUserDetailsByUsername(value: String): UserDetails?

}