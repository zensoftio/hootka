package io.zensoft.sample.exception

import io.zensoft.web.annotation.ControllerAdvice
import io.zensoft.web.annotation.ExceptionHandler
import io.zensoft.web.annotation.ResponseStatus
import io.zensoft.web.api.model.HttpStatus

@ControllerAdvice
class WebExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(values = [IllegalArgumentException::class])
    fun handleIllegalArgument(): String {
        return HttpStatus.BAD_REQUEST.name
    }

}