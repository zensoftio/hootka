package io.zensoft.sample.controller

import io.zensoft.hootka.annotation.*
import io.zensoft.hootka.api.HttpSession
import io.zensoft.hootka.api.SecurityProvider
import io.zensoft.hootka.api.WrappedHttpRequest
import io.zensoft.hootka.api.WrappedHttpResponse
import io.zensoft.hootka.api.model.HttpMethod
import io.zensoft.hootka.api.model.InMemoryFile
import io.zensoft.hootka.api.model.MimeType
import io.zensoft.hootka.api.model.SimpleAuthenticationDetails
import io.zensoft.sample.domain.AuthRequest
import io.zensoft.sample.domain.MultiObject
import io.zensoft.sample.domain.User
import io.zensoft.sample.domain.UserDto
import javax.validation.Valid

@Controller
class SampleController(
    private val securityProvider: SecurityProvider<SimpleAuthenticationDetails>
) {

    @Stateless
    @RequestMapping(method = HttpMethod.GET, value = ["/status"], produces = MimeType.TEXT_PLAIN)
    fun status(): String {
        return "Hello World"
    }

    @Stateless
    @RequestMapping(method = HttpMethod.GET, value = ["/api/greet"], produces = MimeType.TEXT_PLAIN)
    fun greet(@RequestParam name: String?): String {
        return "Greetings, $name!!!"
    }

    @Stateless
    @RequestMapping(method = HttpMethod.POST, value = ["/api/mutate/{firstName}"])
    fun mutate(@RequestBody request: UserDto, @PathVariable firstName: String): UserDto {
        return UserDto(firstName, request.lastName, request.age)
    }

    @Stateless
    @RequestMapping(method = HttpMethod.POST, value = ["/api/user"])
    fun addUser(@ModelAttribute request: UserDto): UserDto {
        return UserDto(request.firstName, request.lastName, request.age)
    }

    @Stateless

    @RequestMapping(method = HttpMethod.POST, value = ["/api/image"], produces = MimeType.TEXT_PLAIN)
    fun addImage(@MultipartFile(acceptExtensions = ["png"]) file: InMemoryFile): String {
        return "File \"${file.name}\" upload success!"
    }

    @Stateless
    @RequestMapping(method = HttpMethod.POST, value = ["/api/describe-image"], produces = MimeType.TEXT_PLAIN)
    fun addImageWithDescription(@MultipartObject(acceptExtensions = ["png"]) multiObject: MultiObject): String {
        return "File \"${multiObject.file.name}\" upload success!\nDescription: \"${multiObject.description}\""
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

    @RequestMapping(method = HttpMethod.POST, value = ["/login"])
    fun login(@ModelAttribute @Valid authRequest: AuthRequest, response: WrappedHttpResponse,
              request: WrappedHttpRequest, session: HttpSession): String {
        val authDetails = SimpleAuthenticationDetails(
            authRequest.username!!,
            authRequest.password!!,
            request,
            response,
            authRequest.rememberMe!!
        )
        securityProvider.authenticate(authDetails)
        return "Login success!"
    }

    @RequestMapping(method = HttpMethod.GET, value = ["/api/principal/secure"], produces = MimeType.TEXT_PLAIN)
    fun principalSecure(@Principal user: User?, request: WrappedHttpRequest, session: HttpSession): String {
        return if (null == user) {
            "Please, login."
        } else {
            "Welcome, ${user.getUsername()}!"
        }
    }

}