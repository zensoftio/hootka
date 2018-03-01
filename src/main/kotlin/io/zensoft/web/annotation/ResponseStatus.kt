package io.zensoft.web.annotation

import io.zensoft.web.support.HttpStatus

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class ResponseStatus(
    val value: HttpStatus
)