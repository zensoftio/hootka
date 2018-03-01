package io.zensoft.web.provider

import io.zensoft.annotation.ControllerAdvice
import io.zensoft.annotation.ExceptionHandler
import io.zensoft.annotation.ResponseStatus
import io.zensoft.web.support.HttpHandlerMetaInfo
import io.zensoft.web.support.HttpStatus
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

    private val exceptionHandlers = HashMap<KClass<out Throwable>, HttpHandlerMetaInfo>()


    fun getExceptionHandler(exceptionType: KClass<out Throwable>): HttpHandlerMetaInfo? {
        return exceptionHandlers[exceptionType]
    }

    @PostConstruct
    private fun init() {
        val advices = context.getBeansWithAnnotation(ControllerAdvice::class.java).values
        for (advice in advices) {
            for (function in advice::class.declaredFunctions) {
                val annotation = function.findAnnotation<ExceptionHandler>() ?: continue
                val parameterMapping = handlerParameterMapperProvider.getHandlerParameters(function)
                val status = function.findAnnotation<ResponseStatus>()?.value ?: HttpStatus.OK
                val handlerMetaInfo = HttpHandlerMetaInfo(advice, function, parameterMapping, false, status)
                for (exceptionType in annotation.values) {
                    if(exceptionHandlers.containsKey(exceptionType)) {
                        throw IllegalStateException("Only one handler should be applied on $exceptionType")
                    }
                    exceptionHandlers[exceptionType] = handlerMetaInfo
                }
            }
        }
    }
}