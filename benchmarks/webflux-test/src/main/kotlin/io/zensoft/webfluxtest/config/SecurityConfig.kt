package io.zensoft.webfluxtest.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain


@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.authorizeExchange()
            .anyExchange().authenticated()
            .and().formLogin()
            .and()
        return http.build()
    }

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): MapReactiveUserDetailsService {
        val user = User
            .withUsername("user")
            .password(passwordEncoder.encode("user"))
            .roles()
            .build()
        return MapReactiveUserDetailsService(user)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(4)
    }

}