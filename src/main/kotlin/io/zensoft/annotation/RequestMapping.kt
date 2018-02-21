package io.zensoft.annotation

import io.zensoft.web.support.HttpMethod

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.CLASS)
annotation class RequestMapping(
        val value: String,
        val method: HttpMethod = HttpMethod.GET
)
