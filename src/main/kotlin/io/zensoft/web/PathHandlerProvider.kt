package io.zensoft.web

import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.annotation.*
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import io.zensoft.web.support.HttpMethod
import io.zensoft.web.support.HttpStatus
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.util.AntPathMatcher
import java.util.*
import javax.annotation.PostConstruct
import kotlin.collections.HashMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaType

@Component
class PathHandlerProvider(
        private val context: ApplicationContext
) {

    private val antPathMatcher = AntPathMatcher()
    private val storage = TreeMap<String, HttpHandlerMetaInfo>()
    private val exceptionHandlers = HashMap<KClass<out Throwable>, HttpHandlerMetaInfo>()

    @PostConstruct
    private fun init() {
        registerHandlerMethods()
        registerExceptionHandlers()
    }

    fun getMethodHandler(request: FullHttpRequest) : HttpHandlerMetaInfo? {
        return storage.keys
                .firstOrNull { antPathMatcher.match(it, request.uri()) }
                ?.let { storage[it] }
    }

    fun getExceptionHandler(exceptionType: KClass<out Throwable>): HttpHandlerMetaInfo? {
        return exceptionHandlers[exceptionType]
    }

    private fun registerHandlerMethods() {
        val beans = context.getBeansWithAnnotation(Controller::class.java).values
        for (bean in beans) {
            val superPathAnnotation = bean::class.findAnnotation<RequestMapping>()
            val superPath = superPathAnnotation?.value ?: ""
            val functions = bean::class.declaredFunctions

            for (function in functions) {
                val pathAnnotation = function.findAnnotation<RequestMapping>() ?: continue
                val path = superPath + pathAnnotation.value
                val parameterMapping = mapHandlerParameters(function)
                val status = function.findAnnotation<ResponseStatus>()?.value ?: HttpStatus.OK
                val type = pathAnnotation.produces
                if (storage.containsKey(pathAnnotation.value)) {
                    throw IllegalStateException("Mapping $path is already exists.")
                } else {
                    storage[path] = HttpHandlerMetaInfo(bean, function, parameterMapping, status, type, path, pathAnnotation.method)
                }
            }
        }
    }

    private fun registerExceptionHandlers() {
        val advices = context.getBeansWithAnnotation(ControllerAdvice::class.java).values
        for (advice in advices) {
            for (function in advice::class.declaredFunctions) {
                val annotation = function.findAnnotation<ExceptionHandler>() ?: continue
                val parameterMapping = mapHandlerParameters(function)
                val handlerMetaInfo = HttpHandlerMetaInfo(advice, function, parameterMapping)
                for (exceptionType in annotation.values) {
                    exceptionHandlers[exceptionType] = handlerMetaInfo
                }
            }
        }
    }

    private fun mapHandlerParameters(function: KFunction<*>): Map<String, HandlerMethodParameter> {
        val parameterMapping = linkedMapOf<String, HandlerMethodParameter>()
        for (parameter in function.valueParameters) {
            if(parameter.annotations.isEmpty()) {
                parameterMapping[parameter.name!!] = HandlerMethodParameter(parameter.type.javaType as Class<*>)
                continue
            }
            parameter.annotations.forEach {
                when(it) {
                    is PathVariable -> {
                        val patternName = if (it.value.isEmpty()) parameter.name else it.value
                        parameterMapping[patternName!!] = HandlerMethodParameter(parameter.type.javaType as Class<*>, it)
                    }
                    is RequestBody -> {
                        parameterMapping[parameter.name!!] = HandlerMethodParameter(parameter.type.javaType as Class<*>, it)
                    }
                    else -> {
                        throw IllegalArgumentException("Unknown annotated parameter: ${parameter.name} in ${function.name} method")
                    }
                }
            }
        }
        return parameterMapping
    }

}