package io.zensoft.hootka.annotation

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PathVariable(
    val value: String = ""
)