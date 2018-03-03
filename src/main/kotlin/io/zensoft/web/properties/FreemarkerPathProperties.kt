package io.zensoft.web.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@ConfigurationProperties("freemarker.path")
@Validated
@Component
class FreemarkerPathProperties(
    val prefix: String = "templates/",
    val suffix: String = ".ftl"
)