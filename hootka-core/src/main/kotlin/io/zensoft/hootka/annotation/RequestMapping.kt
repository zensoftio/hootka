package io.zensoft.hootka.annotation

import io.zensoft.hootka.api.model.HttpMethod
import io.zensoft.hootka.api.model.MimeType

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CLASS)
annotation class RequestMapping(
    val value: Array<String> = [],
    val produces: MimeType = MimeType.APPLICATION_JSON,
    val method: HttpMethod = HttpMethod.GET
)
