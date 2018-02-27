package io.zensoft.controller

import io.zensoft.annotation.ControllerAdvice
import io.zensoft.annotation.ExceptionHandler
import io.zensoft.annotation.ResponseStatus
import io.zensoft.web.support.HttpStatus

@ControllerAdvice
class ExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler([IllegalStateException::class, IllegalArgumentException::class])
    fun handleException(ex: Exception): String {
        return "Something went wrong ${ex.message}"
    }

}