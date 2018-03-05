package io.zensoft.controller

import io.zensoft.web.annotation.*
import io.zensoft.domain.UserDto
import io.zensoft.web.support.*
import org.springframework.stereotype.Controller

@Controller
@RequestMapping(value = "/api/user")
class UserController {

    @RequestMapping(value = "/statefull", method = HttpMethod.GET)
    fun getCurrentUser(): UserDto {
        return UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com")
    }

    @Stateless
    @RequestMapping(value = "/stateless", method = HttpMethod.GET)
    fun getCurrentUserStateless(): UserDto {
        return UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com")
    }

    @Stateless
    @RequestMapping(value = "/current/view/{firstName}", method = HttpMethod.GET)
    fun viewUser(@PathVariable firstName: String, @RequestParam name: String, @RequestParam lastName: String) {
        // return UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com"))
    }

    @Stateless
    @RequestMapping(value = "/doReflect", method = HttpMethod.POST)
    fun reflectUser(@RequestBody request: UserDto): Any? {
        return request
    }

    @RequestMapping(value = "/doReflectForm", method = HttpMethod.POST)
    fun reflectForm(@ModelAttribute user: UserDto): Any? {
        return user
    }

    @RequestMapping(value = "/doAcceptFile", method = HttpMethod.POST)
    fun acceptFile(@MultipartFile file: InMemoryFile): Any? {
        return file.name
    }

    @RequestMapping(value = "/fail", method = HttpMethod.GET)
    fun fail() {
        throw IllegalArgumentException("You wanted it, you got it")
    }

}