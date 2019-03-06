package io.zensoft.sample.domain

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class AuthRequest(
    @field:NotBlank @field:Email var username: String? = null,
    @field:NotBlank var password: String? = null,
    @field:NotNull var rememberMe: Boolean? = null
)