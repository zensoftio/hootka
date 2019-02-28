package io.zensoft.hootka.api.internal.provider

import io.zensoft.hootka.annotation.ControllerAdvice
import io.zensoft.hootka.annotation.ExceptionHandler
import io.zensoft.hootka.annotation.ResponseStatus
import io.zensoft.hootka.api.internal.support.ExceptionHandlerKey
import io.zensoft.hootka.api.internal.support.HttpHandlerMetaInfo
import io.zensoft.hootka.api.model.HttpMethod
import io.zensoft.hootka.api.model.HttpStatus
import io.zensoft.hootka.api.model.MimeType
import org.springframework.context.ApplicationContext
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import javax.annotation.PostConstruct
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

class ExceptionHandlerProvider(
    private val context: ApplicationContext,
    private val handlerParameterMapperProvider: HandlerParameterMapperProvider
) {

    private val exceptionHandlers = HashMap<ExceptionHandlerKey, HttpHandlerMetaInfo>()

    fun getExceptionHandler(exceptionType: KClass<out Throwable>, contentType: MimeType = MimeType.APPLICATION_JSON): HttpHandlerMetaInfo? {
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
                val paramTypes = function.parameters.map { it.type.jvmErasure.java }.drop(1)
                val methodType = MethodType.methodType(function.returnType.jvmErasure.java, paramTypes)
                val methodHandle = MethodHandles.lookup().findVirtual(advice.javaClass, function.name, methodType)
                val handlerMetaInfo = HttpHandlerMetaInfo(advice, methodHandle, parameterMapping,
                    false, status, annotation.produces, "", HttpMethod.GET, null)
                for (exceptionType in annotation.values) {
                    val key = ExceptionHandlerKey(exceptionType, annotation.produces.toString())
                    if (exceptionHandlers.containsKey(key)) {
                        throw IllegalStateException("Only one handler should be applied on $exceptionType")
                    }
                    exceptionHandlers[key] = handlerMetaInfo
                }
            }
        }
    }
}