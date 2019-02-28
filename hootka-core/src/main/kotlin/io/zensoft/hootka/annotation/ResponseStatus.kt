package io.zensoft.hootka.annotation

import io.zensoft.hootka.api.model.HttpStatus

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class ResponseStatus(
    val value: HttpStatus
)