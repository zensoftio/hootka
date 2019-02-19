package io.zensoft.web.api.internal.mapper

import io.zensoft.web.annotation.PathVariable
import io.zensoft.web.api.HttpRequestMapper
import io.zensoft.web.api.internal.support.HandlerMethodParameter
import io.zensoft.web.api.internal.support.HttpHandlerMetaInfo
import io.zensoft.web.api.internal.support.RequestContext
import io.zensoft.web.api.internal.utils.NumberUtils
import org.springframework.util.AntPathMatcher
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

class PathVariableMapper : HttpRequestMapper {

    private val pathMatcher = AntPathMatcher()

    override fun supportsAnnotation(annotations: List<Annotation>): Boolean {
        return annotations.find { it is PathVariable } != null
    }

    override fun createValue(parameter: HandlerMethodParameter, context: RequestContext, handlerMethod: HttpHandlerMetaInfo): Any {
        val pathVariables = pathMatcher.extractUriTemplateVariables(handlerMethod.path, context.request.getPath())
        return NumberUtils.parseNumber(pathVariables[parameter.name]!!, parameter.clazz)
    }

    override fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter {
        val annotation = annotations.find { it is PathVariable } as PathVariable
        val patternName = if (annotation.value.isEmpty()) parameter.name else annotation.value
        return HandlerMethodParameter(patternName!!, parameter.type.javaType as Class<*>,
            parameter.type.isMarkedNullable, annotation)
    }

}