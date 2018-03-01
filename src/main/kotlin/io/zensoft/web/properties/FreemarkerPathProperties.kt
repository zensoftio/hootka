package io.zensoft.web.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "freemarker.path", ignoreUnknownFields = false)
@Validated
@Component
class FreemarkerPathProperties(
    val prefix: String = "templates/",
    val suffix: String = ".ftl"
)