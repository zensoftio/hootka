package io.zensoft.sample.config

import io.zensoft.web.api.SecurityExpressionInitializer
import io.zensoft.web.api.UserDetails
import io.zensoft.web.api.internal.security.RootSecurityExpressions
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

}

class CustomSecurityExpression: RootSecurityExpressions() {

    fun nobody(): Boolean {
        return false
    }

}