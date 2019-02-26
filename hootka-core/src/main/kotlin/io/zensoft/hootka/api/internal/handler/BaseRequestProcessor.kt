package io.zensoft.hootka.api.internal.handler

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.util.AsciiString
import io.zensoft.hootka.api.HttpSession
import io.zensoft.hootka.api.SessionHandler
import io.zensoft.hootka.api.WrappedHttpRequest
import io.zensoft.hootka.api.WrappedHttpResponse
import io.zensoft.hootka.api.exceptions.HandlerMethodNotFoundException
import io.zensoft.hootka.api.exceptions.HandlerParameterInstantiationException
import io.zensoft.hootka.api.exceptions.PreconditionNotSatisfiedException
import io.zensoft.hootka.api.internal.provider.*
import io.zensoft.hootka.api.internal.security.SecurityExpressionExecutor
import io.zensoft.hootka.api.internal.support.HttpHandlerMetaInfo
import io.zensoft.hootka.api.internal.support.RequestContext
import io.zensoft.hootka.api.internal.utils.DeserializationUtils
import io.zensoft.hootka.api.model.HttpStatus
import io.zensoft.hootka.api.model.MimeType
import org.apache.commons.lang3.StringUtils
import java.lang.reflect.InvocationTargetException

class BaseRequestProcessor(
    private val pathHandlerProvider: MethodHandlerProvider,
    private val exceptionHandlerProvider: ExceptionHandlerProvider,
    private val sessionHandler: SessionHandler,
    private val handlerParameterProvider: HandlerParameterMapperProvider,
    private val responseResolverProvider: ResponseResolverProvider,
    private val staticResourcesProvider: StaticResourcesProvider,
    private val preconditionsProvider: SecurityExpressionExecutor? = null
) {

    companion object {
        private const val REDIRECT_PREFIX = "redirect:"
    }

    fun processRequest(wrappedRequest: WrappedHttpRequest, wrappedResponse: WrappedHttpResponse) {
        var handler: HttpHandlerMetaInfo? = null
        var context: RequestContext? = null
        try {
            context = RequestContext(wrappedRequest, wrappedResponse)
            handler = pathHandlerProvider.getMethodHandler(wrappedRequest.getPath(), wrappedRequest.getMethod())
            if (!handler.stateless) {
                context.session = sessionHandler.getOrCreateSession(wrappedRequest, wrappedResponse)
            }
            handler.preconditionExpression?.let {
                preconditionsProvider?.checkAllowance(it, context)
            }
            handleRequest(handler, context)
        } catch (ex: InvocationTargetException) {
            handleException(handler!!.contentType, ex.targetException!!, context!!)
        } catch (ex: HandlerMethodNotFoundException) {
            if (!staticResourcesProvider.handleStaticResource(wrappedRequest, wrappedResponse)) {
                handleException(MimeType.TEXT_HTML, ex, context!!)
            }
        } catch (ex: PreconditionNotSatisfiedException) {
            handleException(handler!!.contentType, ex, context!!)
        } catch (ex: HandlerParameterInstantiationException) {
            handleException(handler!!.contentType, ex, context!!)
        } catch (ex: Exception) {
            handleException(handler!!.contentType, ex, context!!)
        }
    }

    private fun handleRequest(handler: HttpHandlerMetaInfo, context: RequestContext) {
        val args = createHandlerArguments(handler, context)
        val result = handler.execute(*args)
        if (!isRedirectResponse(context.response, result, handler)) {
            val responseBody = result?.let {
                responseResolverProvider.createResponseBody(result, args, handler.contentType, context.response)
            }
            context.response.mutate(handler.status, handler.contentType, responseBody)
        }
    }

    private fun handleException(contentType: MimeType, exception: Throwable, context: RequestContext) {
        val exceptionHandler = exceptionHandlerProvider.getExceptionHandler(exception::class, contentType)
            ?: exceptionHandlerProvider.getExceptionHandler(exception::class)
        if (exceptionHandler != null) {
            val args = createHandlerArguments(exceptionHandler, context, exception)
            val result = exceptionHandler.execute(*args)
            if (!isRedirectResponse(context.response, result, exceptionHandler)) {
                try {
                    val responseBody = responseResolverProvider.createResponseBody(result!!, args, exceptionHandler.contentType, context.response)
                    context.response.mutate(exceptionHandler.status, exceptionHandler.contentType, responseBody)
                } catch (ex: Exception) {
                    context.response.mutate(HttpStatus.INTERNAL_SERVER_ERROR, MimeType.TEXT_PLAIN,
                        (exception.message ?: StringUtils.EMPTY).toByteArray())
                }
            }
        } else {
            context.response.mutate(HttpStatus.INTERNAL_SERVER_ERROR, MimeType.TEXT_PLAIN,
                (exception.message ?: StringUtils.EMPTY).toByteArray())
        }
    }

    private fun isRedirectResponse(response: WrappedHttpResponse, result: Any?, handler: HttpHandlerMetaInfo): Boolean {
        if (result is String && result.startsWith(REDIRECT_PREFIX)) {
            response.mutate(HttpStatus.FOUND, handler.contentType)
            val pathIdx = result.indexOf(REDIRECT_PREFIX) + REDIRECT_PREFIX.count()
            response.setHeader(HttpHeaderNames.LOCATION.toString(), AsciiString(result.substring(pathIdx)).toString())
            return true
        }
        return false
    }

    private fun createHandlerArguments(handler: HttpHandlerMetaInfo, context: RequestContext, exception: Throwable? = null): Array<Any?> {
        val result = handler.parameters.map {
            when {
                it.annotation != null -> handlerParameterProvider.createParameterValue(it, context, handler)
                else -> defineContextParameter(it.clazz, context, exception)
            }
        }
        return result.toTypedArray()
    }

    private fun defineContextParameter(parameterType: Class<*>, context: RequestContext, exception: Throwable? = null): Any {
        return when {
            WrappedHttpRequest::class.java == parameterType -> context.request
            WrappedHttpResponse::class.java == parameterType -> context.response
            HttpSession::class.java == parameterType -> context.session
                ?: throw IllegalStateException("Session not found")
            Throwable::class.java.isAssignableFrom(parameterType) -> exception
                ?: throw IllegalStateException("Unknown exception specified")
            else -> DeserializationUtils.createBeanFromQueryString(parameterType, context.request.getQueryParameters())
        }
    }

}