package io.zensoft.web.default

import io.zensoft.web.annotation.ControllerAdvice
import io.zensoft.web.annotation.ExceptionHandler
import io.zensoft.web.annotation.ResponseStatus
import io.zensoft.web.exception.HandlerMethodNotFoundException
import io.zensoft.web.exception.PreconditionNotSatisfiedException
import io.zensoft.web.support.HttpStatus
import io.zensoft.web.support.MimeType
import io.zensoft.web.support.ViewModel
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
    @ExceptionHandler(values = [HandlerMethodNotFoundException::class], produces = MimeType.TEXT_HTML)
    fun handleMethodHandlerNotFoundException(): String {
        return "handler_not_found"
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(values = [PreconditionNotSatisfiedException::class], produces = MimeType.TEXT_HTML)
    fun handlePreconditionNotSatisfiedException(): String {
        return "forbidden"
    }



}