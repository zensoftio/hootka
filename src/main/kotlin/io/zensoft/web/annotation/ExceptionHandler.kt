package io.zensoft.web.annotation

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.CLASS)
annotation class ExceptionHandler(
    val values: Array<KClass<out Throwable>>
)