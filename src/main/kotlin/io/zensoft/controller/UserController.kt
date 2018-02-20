package io.zensoft.controller

import io.zensoft.annotation.HttpBody
import io.zensoft.annotation.RequestMapping
import io.zensoft.domain.UserDto
import io.zensoft.web.support.HttpMethod
import org.springframework.stereotype.Controller

@Controller
class UserController {

    @RequestMapping(value = "/api/user/current", method = HttpMethod.GET)
    fun getCurrentUser(): Any? {
        return UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com")
    }

    @RequestMapping(value = "/api/user/doReflect", method = HttpMethod.POST)
    fun reflectUser(@HttpBody request: UserDto): Any? {
        return request
    }

}