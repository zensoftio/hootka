package io.zensoft.web.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(value = "freemarker.path", ignoreUnknownFields = false)
@Validated
@Component
class FreemarkerPathProperties(
    var prefix: String = "",
    var suffix: String = ""
)