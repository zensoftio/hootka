package io.zensoft.controller

import io.zensoft.annotation.HttpBody
import io.zensoft.annotation.PathVariable
import io.zensoft.annotation.RequestMapping
import io.zensoft.domain.UserDto
import io.zensoft.web.support.HttpMethod
import org.springframework.stereotype.Controller

@Controller
@RequestMapping(value = "/api/user")
class UserController {

    @RequestMapping(value = "/{name}/{surname}/{age}", method = HttpMethod.GET)
    fun getCurrentUser(@PathVariable name: String, @PathVariable age: Int, @PathVariable surname: String): Any? {
        return UserDto(name, "Molchanov", "ruslanys@gmail.com")
    }

    @RequestMapping(value = "/api/user/doReflect", method = HttpMethod.POST)
    fun reflectUser(@HttpBody request: UserDto): Any? {
        return request
    }

}