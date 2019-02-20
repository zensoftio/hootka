package io.zensoft.tomcattest.rpc.controller

import io.zensoft.tomcattest.dto.UserDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class TomcatTestController {

    @GetMapping
    fun testTomcat(): UserDto = UserDto("Alice", "Johnson")

}