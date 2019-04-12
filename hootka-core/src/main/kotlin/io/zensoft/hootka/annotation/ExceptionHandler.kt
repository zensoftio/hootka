package io.zensoft.hootka.annotation

import io.zensoft.hootka.api.model.MimeType
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.CLASS)
annotation class ExceptionHandler(
    val values: Array<KClass<out Throwable>>,
    val produces: MimeType = MimeType.APPLICATION_JSON
)