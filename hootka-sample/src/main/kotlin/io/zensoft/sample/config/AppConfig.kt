package io.zensoft.sample.config

import io.zensoft.hootka.api.SecurityExpressionInitializer
import io.zensoft.hootka.api.UserDetails
import io.zensoft.hootka.api.UserDetailsService
import io.zensoft.hootka.api.internal.security.RootSecurityExpressions
import io.zensoft.hootka.default.DefaultExceptionControllerAdvice
import io.zensoft.sample.domain.Role
import io.zensoft.sample.domain.User
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean
    fun securityExpressionInitializer(): SecurityExpressionInitializer = object : SecurityExpressionInitializer {
        override fun createSecurityExpressions(principal: UserDetails?): RootSecurityExpressions {
            return CustomSecurityExpression()
        }
    }

    @Bean
    fun defaultExceptionControllerAdvice(): DefaultExceptionControllerAdvice {
        return DefaultExceptionControllerAdvice()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        return object : UserDetailsService {
            override fun findUserDetailsByUsername(value: String): UserDetails? {
                val email = "some@gmail.com"
                return if (value == email) {
                    User(email, "\$2a\$10\$P.mTV9OcadADM0PhFzaMueexn3lrUV.D01CnmTi4uxdGMdrISN8wK", setOf(Role("ADMIN")), true)
                } else null
            }
        }
    }

}

class CustomSecurityExpression : RootSecurityExpressions() {

    fun nobody(): Boolean {
        return false
    }

}