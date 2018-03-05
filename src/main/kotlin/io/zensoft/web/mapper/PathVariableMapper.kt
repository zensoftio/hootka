package io.zensoft.web.mapper

import io.zensoft.web.annotation.PathVariable
import io.zensoft.web.utils.NumberUtils
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import io.zensoft.web.support.WrappedHttpRequest
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

@Component
class PathVariableMapper: HttpRequestMapper {

    private val pathMatcher = AntPathMatcher()

    override fun supportsAnnotation(annotations: List<Annotation>): Boolean {
        return annotations.find { it is PathVariable } != null
    }

    override fun createValue(parameter: HandlerMethodParameter, request: WrappedHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any {
        val pathVariables = pathMatcher.extractUriTemplateVariables(handlerMethod.path, request.path)
        return NumberUtils.parseNumber(pathVariables[parameter.name]!!, parameter.clazz)
    }

    override fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter {
        val annotation = annotations.find { it is PathVariable } as PathVariable
        val patternName = if (annotation.value.isEmpty()) parameter.name else annotation.value
        return HandlerMethodParameter(patternName!!, parameter.type.javaType as Class<*>,
            parameter.type.isMarkedNullable, annotation)
    }

}