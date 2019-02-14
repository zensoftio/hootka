package io.zensoft.web.api.internal.provider

import io.zensoft.web.api.HttpRequestMapper
import io.zensoft.web.api.ValidationProvider
import io.zensoft.web.api.exceptions.HandlerParameterInstantiationException
import io.zensoft.web.api.internal.support.HandlerMethodParameter
import io.zensoft.web.api.internal.support.HttpHandlerMetaInfo
import io.zensoft.web.api.internal.support.RequestContext
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
    private val validationService: ValidationProvider
) {

    private lateinit var mappers: List<HttpRequestMapper>

    fun createParameterValue(parameter: HandlerMethodParameter, context: RequestContext, handlerMethod: HttpHandlerMetaInfo): Any? {
        for (mapper in mappers) {
            if (mapper.supportsAnnotation(listOf(parameter.annotation!!))) {
                val argument = try {
                    mapper.createValue(parameter, context, handlerMethod)
                } catch (ex: Exception) {
                    throw HandlerParameterInstantiationException(ex.message)
                }
                if (argument != null && parameter.validationRequired) {
                    validationService.validate(argument)
                }
                return argument
            }
        }
        throw IllegalArgumentException("Unknown annotation type to map handler parameter. Annotation: ${parameter.annotation}")
    }

    private fun mapHandlerParameter(parameter: KParameter): HandlerMethodParameter {
        val annotations = parameter.annotations
        mappers
            .filter { it.supportsAnnotation(annotations) }
            .forEach { return it.mapParameter(parameter, annotations) }
        throw IllegalArgumentException("Unknown annotated parameter: ${parameter.name}")
    }

    fun mapHandlerParameters(function: KFunction<*>): List<HandlerMethodParameter> {
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