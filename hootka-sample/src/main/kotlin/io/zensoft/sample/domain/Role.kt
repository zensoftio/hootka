package io.zensoft.sample.domain

import io.zensoft.hootka.api.UserAuthority

class Role(
    var key: String
) : UserAuthority {
    override fun getAuthority(): String = key
}