package io.zensoft.controller

import io.zensoft.web.annotation.*
import io.zensoft.domain.UserDto
import io.zensoft.web.api.model.HttpMethod
import io.zensoft.web.api.model.InMemoryFile
import io.zensoft.web.api.model.MimeType
import io.zensoft.web.api.model.ViewModel

@Controller
@RequestMapping(value = "/api/user")
class UserController {

    @AllowInCase("roles.hasRole('USER', session)")
    @RequestMapping(value = "/stateful", method = HttpMethod.GET)
    fun getCurrentUser(): UserDto {
        return UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com")
    }

    @Stateless
    @RequestMapping(value = "/stateless", method = HttpMethod.GET)
    fun getCurrentUserStateless(): UserDto {
        return UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com")
    }

    @RequestMapping(value = "/search", method = HttpMethod.GET)
    fun search(user: UserDto): UserDto {
        return user
    }

    @Stateless
    @RequestMapping(value = "/current/view/{firstName}", method = HttpMethod.GET, produces = MimeType.TEXT_HTML)
    fun viewUser(@PathVariable firstName: String, @RequestParam name: String, @RequestParam lastName: String, viewModel: ViewModel): String {
        viewModel.setAttribute("user", UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com"))
        return "somepage"
    }

    @Stateless
    @RequestMapping(value = "/redirect", method = HttpMethod.GET)
    fun viewUser(): String {
        return "redirect:/api/user/stateless"
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