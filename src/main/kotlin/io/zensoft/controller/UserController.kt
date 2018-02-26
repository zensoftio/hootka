package io.zensoft.controller

import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.annotation.RequestBody
import io.zensoft.annotation.PathVariable
import io.zensoft.annotation.RequestMapping
import io.zensoft.domain.UserDto
import io.zensoft.web.support.HttpMethod
import io.zensoft.web.support.Session
import org.springframework.stereotype.Controller

@Controller
@RequestMapping(value = "/api/user")
class UserController {

    @RequestMapping(value = "/{name}", method = HttpMethod.GET)
    fun getCurrentUser(@PathVariable name: String, session: Session, request: FullHttpRequest): UserDto? {
        return UserDto(name, "Molchanov", "ruslanys@gmail.com")
    }

    @RequestMapping(value = "/doReflect", method = HttpMethod.POST)
    fun reflectUser(@RequestBody request: UserDto): Any? {
        return request
    }

}