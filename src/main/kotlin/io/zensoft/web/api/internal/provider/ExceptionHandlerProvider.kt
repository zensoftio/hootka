package io.zensoft.web.api.internal.provider

import io.zensoft.web.annotation.ControllerAdvice
import io.zensoft.web.annotation.ExceptionHandler
import io.zensoft.web.annotation.ResponseStatus
import io.zensoft.web.api.model.HttpMethod
import io.zensoft.web.api.model.HttpStatus
import io.zensoft.web.api.model.MimeType
import io.zensoft.web.api.internal.support.ExceptionHandlerKey
import io.zensoft.web.api.internal.support.HttpHandlerMetaInfo
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation

@Component
class ExceptionHandlerProvider(
    private val context: ApplicationContext,
    private val handlerParameterMapperProvider: HandlerParameterMapperProvider
) {

    private val exceptionHandlers = HashMap<ExceptionHandlerKey, HttpHandlerMetaInfo>()

    fun getExceptionHandler(exceptionType: KClass<out Throwable>, contentType: MimeType): HttpHandlerMetaInfo? {
        val key = ExceptionHandlerKey(exceptionType, contentType.toString())
        return exceptionHandlers[key]
    }

    @PostConstruct
    private fun init() {
        val advices = context.getBeansWithAnnotation(ControllerAdvice::class.java).values
        for (advice in advices) {
            for (function in advice::class.declaredFunctions) {
                val annotation = function.findAnnotation<ExceptionHandler>() ?: continue
                val parameterMapping = handlerParameterMapperProvider.mapHandlerParameters(function)
                val status = function.findAnnotation<ResponseStatus>()?.value ?: HttpStatus.OK
                val handlerMetaInfo = HttpHandlerMetaInfo(advice, function, parameterMapping,
                    false, status, annotation.produces, "", HttpMethod.GET, null)
                for (exceptionType in annotation.values) {
                    val key = ExceptionHandlerKey(exceptionType, annotation.produces.toString())
                    if(exceptionHandlers.containsKey(key)) {
                        throw IllegalStateException("Only one handler should be applied on $exceptionType")
                    }
                    exceptionHandlers[key] = handlerMetaInfo
                }
            }
        }
    }
}