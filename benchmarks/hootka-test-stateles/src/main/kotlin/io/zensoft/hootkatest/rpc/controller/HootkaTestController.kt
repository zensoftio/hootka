package io.zensoft.hootkatest.rpc.controller

import io.zensoft.hootkatest.dto.UserDto
import io.zensoft.web.annotation.Controller
import io.zensoft.web.annotation.RequestMapping
import io.zensoft.web.annotation.Stateless
import io.zensoft.web.api.model.HttpMethod

@Controller
@RequestMapping(["/api/user"])
class HootkaTestController {

    @Stateless
    @RequestMapping(method = HttpMethod.GET)
    fun testWebflux(): UserDto = UserDto("Alice", "Johnson")

}