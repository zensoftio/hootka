package io.zensoft.controller

import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.annotation.RequestMapping
import io.zensoft.domain.UserDto
import org.springframework.stereotype.Controller

@Controller
class UserController {

    @RequestMapping("/api/user/current")
    fun getCurrentUser(request: FullHttpRequest): Any? {
        return UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com")
    }

}