package io.zensoft.web.annotation

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MultipartObject(
    val acceptExtensions: Array<String> = []
)