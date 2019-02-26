package io.zensoft.sample.controller

import io.zensoft.sample.domain.UserDto
import io.zensoft.web.annotation.*
import io.zensoft.web.api.model.HttpMethod

@Controller
class SampleController {

    @Stateless
    @RequestMapping(method = HttpMethod.GET, value = ["/api/greet"])
    fun greet(@RequestParam name: String): String {
        return "Greetings, $name!!!"
    }

    @Stateless
    @RequestMapping(method = HttpMethod.POST, value = ["/api/mutate/{firstName}"])
    fun mutate(@RequestBody request: UserDto, @PathVariable firstName: String): UserDto {
        return UserDto(firstName, request.lastName, request.age)
    }

    @Stateless
    @RequestMapping(method = HttpMethod.GET, value = ["/api/fail"])
    fun fail() {
        throw IllegalArgumentException()
    }

    @PreAuthorize("nobody()")
    @RequestMapping(method = HttpMethod.GET, value = ["/api/secure"])
    fun secure(): UserDto {
        return UserDto("John", "Doe", 22)
    }

}