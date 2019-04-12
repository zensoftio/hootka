package io.zensoft.sample.exception

import io.zensoft.hootka.annotation.ControllerAdvice
import io.zensoft.hootka.annotation.ExceptionHandler
import io.zensoft.hootka.annotation.ResponseStatus
import io.zensoft.hootka.api.model.HttpStatus

@ControllerAdvice
class WebExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(values = [IllegalArgumentException::class])
    fun handleIllegalArgument(): String {
        return HttpStatus.BAD_REQUEST.name
    }

}