package io.zensoft.web.api.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "web")
@Validated
@Component
class WebConfig(
    var session: SessionConfig = SessionConfig(),
    var security: SecurityConfig = SecurityConfig()
)

class SessionConfig(
    var cookieName: String = "session_id",
    var cookieMaxAge: Long = 1800
)

class SecurityConfig(
    var rememberMeTokenName: String = "remind_token",
    var rememberMeTokenMaxAge: Long = 2592000,
    var rememberMeSalt: String = "default_salt"
)