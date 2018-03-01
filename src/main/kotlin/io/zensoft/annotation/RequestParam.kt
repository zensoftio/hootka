package io.zensoft.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class RequestParam(
    val defaultValue: String = ""
)