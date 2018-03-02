package io.zensoft.web.provider

import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.web.mapper.HttpRequestMapper
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import io.zensoft.web.validation.ValidationService
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaType

@Component
class HandlerParameterMapperProvider(
    private val context: ApplicationContext,
    private val validationService: ValidationService
) {

    private lateinit var mappers: List<HttpRequestMapper>

    fun createParameterValue(parameter: HandlerMethodParameter, request: FullHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any? {
        for (mapper in mappers) {
            if (mapper.supportsAnnotation(parameter.annotation!!)) {
                val argument = mapper.mapValue(parameter, request, handlerMethod)
                if (argument != null && parameter.validationRequired) {
                    validationService.validateBean(argument)
                }
                return argument
            }
        }
        throw IllegalArgumentException("Unknown annotation type to map handler parameter. Annotation: ${parameter.annotation}")
    }

    private fun mapHandlerParameter(parameter: KParameter): HandlerMethodParameter {
        for (annotation in parameter.annotations) {
            for (mapper in mappers) {
                if (mapper.supportsAnnotation(annotation)) {
                    return mapper.mapParameter(parameter, parameter.annotations)
                }
            }
        }
        throw IllegalArgumentException("Unknown annotated parameter: ${parameter.name}")
    }

    fun getHandlerParameters(function: KFunction<*>): List<HandlerMethodParameter> {
        val parameters = mutableListOf<HandlerMethodParameter>()
        for (parameter in function.valueParameters) {
            if (parameter.annotations.isEmpty()) {
                parameters.add(HandlerMethodParameter(parameter.name!!, parameter.type.javaType as Class<*>,
                    parameter.type.isMarkedNullable))
            } else {
                parameters.add(mapHandlerParameter(parameter))
            }
        }
        return parameters
    }

    @PostConstruct
    private fun init() {
        mappers = context.getBeansOfType(HttpRequestMapper::class.java).values.toList()
    }

}