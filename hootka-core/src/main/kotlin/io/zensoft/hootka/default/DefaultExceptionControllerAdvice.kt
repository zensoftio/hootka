package io.zensoft.hootka.default

import io.zensoft.hootka.annotation.ControllerAdvice
import io.zensoft.hootka.annotation.ExceptionHandler
import io.zensoft.hootka.annotation.ResponseStatus
import io.zensoft.hootka.api.HttpSession
import io.zensoft.hootka.api.WrappedHttpRequest
import io.zensoft.hootka.api.exceptions.*
import io.zensoft.hootka.api.model.ExceptionResponse
import io.zensoft.hootka.api.model.HttpStatus
import io.zensoft.hootka.api.model.MimeType
import io.zensoft.hootka.api.model.ValidationError
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException

@ControllerAdvice
class DefaultExceptionControllerAdvice {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

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
    fun handlePreconditionNotSatisfiedException(ex: PreconditionNotSatisfiedException, request: WrappedHttpRequest, session: HttpSession): String {
        return if (ex.viewLogin) {
            session.setAttribute("override_referrer", request.getPath())
            "redirect:/login"
        } else {
            "forbidden"
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(values = [PreconditionNotSatisfiedException::class], produces = MimeType.APPLICATION_JSON)
    fun handlePreconditionNotSatisfiedExceptionAsJson(ex: PreconditionNotSatisfiedException): ExceptionResponse {
        return ExceptionResponse(HttpStatus.FORBIDDEN.value.code(), ex.message)
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(values = [InvalidRememberMeTokenException::class], produces = MimeType.TEXT_HTML)
    fun handleInvalidRememberMeTokenException(ex: InvalidRememberMeTokenException): String {
        log.warn(ex.message)
        return "redirect:/login"
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(values = [AuthenticationFailedException::class], produces = MimeType.TEXT_HTML)
    fun handleAuthenticationFailedExceptionAsHtml(ex: AuthenticationFailedException): String {
        log.warn(ex.message)
        return "unauthorized"
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(values = [AuthenticationFailedException::class], produces = MimeType.APPLICATION_JSON)
    fun handleAuthenticationFailedExceptionAsJson(ex: AuthenticationFailedException): ExceptionResponse {
        log.warn(ex.message)
        return ExceptionResponse(HttpStatus.UNAUTHORIZED.value.code(), ex.message)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(values = [HandlerParameterInstantiationException::class], produces = MimeType.APPLICATION_JSON)
    fun handleAuthenticationFailedExceptionAsJson(ex: HandlerParameterInstantiationException): ExceptionResponse {
        log.warn(ex.message)
        return ExceptionResponse(HttpStatus.BAD_REQUEST.value.code(), ex.message)
    }

}