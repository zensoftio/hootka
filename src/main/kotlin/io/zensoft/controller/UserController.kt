package io.zensoft.controller

import io.zensoft.web.annotation.*
import io.zensoft.domain.UserDto
import io.zensoft.web.support.*
import org.springframework.stereotype.Controller
import javax.validation.Valid

@Controller
@RequestMapping(value = "/api/user")
class UserController {

    @Stateless
    @RequestMapping(value = "/current", method = HttpMethod.GET)
    fun getCurrentUser(): UserDto {
        return UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com")
    }

    @RequestMapping(value = "/current/view", method = HttpMethod.GET, produces = MimeType.TEXT_HTML)
    fun viewUser(viewModel: ViewModel): String? {
        viewModel.setAttribute("user", UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com"))
        return "somepage"
    }

    @Stateless
    @RequestMapping(value = "/doReflect", method = HttpMethod.POST)
    fun reflectUser(@Valid request: UserDto): Any? {
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