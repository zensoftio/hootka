package io.zensoft.webfluxtest.rpc.controller

import io.zensoft.webfluxtest.dto.UserDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/user")
class WebfluxTestController {

    @GetMapping
    fun testWebflux(): UserDto = UserDto("Alice", "Johnson")

}