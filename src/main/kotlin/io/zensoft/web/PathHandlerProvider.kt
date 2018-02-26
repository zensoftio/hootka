package io.zensoft.web

import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.annotation.RequestBody
import io.zensoft.annotation.PathVariable
import io.zensoft.annotation.RequestMapping
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.util.AntPathMatcher
import java.util.*
import javax.annotation.PostConstruct
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaType

@Component
class PathHandlerProvider(
        private val context: ApplicationContext
) {

    private val antPathMatcher = AntPathMatcher()
    private val storage = TreeMap<String, HttpHandlerMetaInfo>()

    @PostConstruct
    private fun init() {
        val beans = context.getBeansWithAnnotation(Controller::class.java).values
        for (bean in beans) {
            val superPathAnnotation = bean::class.findAnnotation<RequestMapping>()
            val superPath = superPathAnnotation?.value ?: ""
            val functions = bean::class.declaredFunctions

            for (function in functions) {
                val pathAnnotation = function.findAnnotation<RequestMapping>() ?: continue
                val path = superPath + pathAnnotation.value
                val parameterMapping = mapHandlerParameters(function)
                if (storage.containsKey(pathAnnotation.value)) {
                    throw IllegalStateException("Mapping $path is already exists.")
                } else {
                    storage[path] = HttpHandlerMetaInfo(bean, function, path, parameterMapping, pathAnnotation.method)
                }
            }
        }
    }

    fun getHandler(request: FullHttpRequest) : HttpHandlerMetaInfo? {
        return storage.keys
                .firstOrNull { antPathMatcher.match(it, request.uri()) }
                ?.let { storage[it] }
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