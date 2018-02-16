package io.zensoft.loc.controller

import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.loc.annotation.RequestMapping
import io.zensoft.loc.domain.UserDto
import org.springframework.stereotype.Controller

@Controller
class UserController {

    @RequestMapping("/api/user/current")
    fun getCurrentUser(request: FullHttpRequest): Any? {
        return UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com")
    }

}