package io.zensoft.controller

import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.web.annotation.*
import io.zensoft.domain.UserDto
import io.zensoft.web.support.HttpMethod
import io.zensoft.web.support.InMemoryFile
import io.zensoft.web.support.MimeType
import io.zensoft.web.support.ViewModel
import org.springframework.stereotype.Controller
import javax.validation.Valid

@Controller
@RequestMapping(value = "/api/user")
class UserController {

    @Stateless
    @RequestMapping(value = "/current", method = HttpMethod.GET)
    fun getCurrentUser(request: FullHttpRequest): UserDto {
        return UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com")
    }

    @RequestMapping(value = "/current/view", method = HttpMethod.GET, produces = MimeType.TEXT_HTML)
    fun viewUser(viewModel: ViewModel): String? {
        viewModel.setAttribute("user", UserDto("Ruslan", "Molchanov", "ruslanys@gmail.com"))
        return "somepage"
    }

    @RequestMapping(value = "/doReflect", method = HttpMethod.POST)
    fun reflectUser(@Valid @RequestBody request: UserDto): Any? {
        return request
    }

    @RequestMapping(value = "/doReflectForm", method = HttpMethod.POST)
    fun reflectForm(@RequestParam firstName: String): Any? {
        return firstName
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