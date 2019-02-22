package io.zensoft.web.autoconfigure.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(value = "hootka", ignoreUnknownFields = true)
@Validated
@Component
class WebConfig(
    val port: Int = 8080,
    val session: SessionConfig = SessionConfig(),
    val security: SecurityConfig = SecurityConfig(),
    val static: StaticConfig = StaticConfig(),
    val freemarker: FreemarkerPathProperties = FreemarkerPathProperties()
)

class SessionConfig(
    var cookieName: String = "session_id",
    var cookieMaxAge: Long = 1800,
    val redis: JedisConfig = JedisConfig()
)

class JedisConfig(
    var host: String = "localhost",
    var port: Int = 6379
)

class SecurityConfig(
    var rememberMeTokenName: String = "remind_token",
    var rememberMeTokenMaxAge: Long = 2592000,
    var rememberMeSalt: String = "default_salt"
)

class StaticConfig(
    var cachedResourceExpiry: Long = 28800
)

class FreemarkerPathProperties(
    var prefix: String = "templates/",
    var suffix: String = ".ftl"
)