package io.zensoft.web.autoconfigure.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(value = "web")
@Validated
@Component
class WebConfig(
    var port: Int = 8080,
    var session: SessionConfig = SessionConfig(),
    var security: SecurityConfig = SecurityConfig(),
    var static: StaticConfig = StaticConfig()
)

class SessionConfig(
    var cookieName: String = "session_id",
    var cookieMaxAge: Long = 1800
)

class SecurityConfig(
    var enabled: Boolean = false
    var rememberMeTokenName: String = "remind_token",
    var rememberMeTokenMaxAge: Long = 2592000,
    var rememberMeSalt: String = "default_salt"
)

class StaticConfig(
    var cachedResourceExpiry: Long = 28800
)