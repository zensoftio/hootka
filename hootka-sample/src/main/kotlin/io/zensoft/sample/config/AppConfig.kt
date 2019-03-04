package io.zensoft.sample.config

import io.zensoft.hootka.api.SecurityExpressionInitializer
import io.zensoft.hootka.api.UserDetails
import io.zensoft.hootka.api.internal.security.RootSecurityExpressions
import io.zensoft.hootka.default.DefaultExceptionControllerAdvice
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean
    fun securityExpressionInitializer(): SecurityExpressionInitializer
        = object: SecurityExpressionInitializer {
            override fun createSecurityExpressions(principal: UserDetails?): RootSecurityExpressions {
                return CustomSecurityExpression()
            }
        }

    @Bean
    fun defaultExceptionControllerAdvice(): DefaultExceptionControllerAdvice {
        return DefaultExceptionControllerAdvice()
    }

}

class CustomSecurityExpression: RootSecurityExpressions() {

    fun nobody(): Boolean {
        return false
    }

}