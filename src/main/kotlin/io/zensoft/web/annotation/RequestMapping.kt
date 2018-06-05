package io.zensoft.web.annotation

import io.zensoft.web.api.model.HttpMethod
import io.zensoft.web.api.model.MimeType

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CLASS)
annotation class RequestMapping(
    val value: String,
    val produces: MimeType = MimeType.APPLICATION_JSON,
    val method: HttpMethod = HttpMethod.GET
)
