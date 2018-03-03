package io.zensoft.web.default

import io.zensoft.web.annotation.ControllerAdvice
import io.zensoft.web.annotation.ExceptionHandler
import io.zensoft.web.annotation.ResponseStatus
import io.zensoft.web.exception.HandlerMethodNotFoundException
import io.zensoft.web.support.HttpStatus
import io.zensoft.web.validation.ValidationError
import javax.validation.ConstraintViolationException

@ControllerAdvice
class DefaultExceptionControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler([ConstraintViolationException::class])
    fun handleValidationException(ex: ConstraintViolationException): List<ValidationError> {
        return ex.constraintViolations.map { ValidationError(it.propertyPath.toString(), it.message) }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler([HandlerMethodNotFoundException::class])
    fun handleMethodHandlerNotFoundException(ex: HandlerMethodNotFoundException) {}


}