package io.zensoft.domain

import org.springframework.util.DigestUtils
import javax.validation.constraints.Size

class UserDto(firstName: String, lastName: String, email: String) {

    @Size(max = 15) val name: String = "$firstName $lastName"
    val emailHash: String = DigestUtils.md5DigestAsHex(email.toByteArray())

}