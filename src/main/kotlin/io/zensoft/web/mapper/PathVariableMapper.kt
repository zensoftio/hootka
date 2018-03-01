package io.zensoft.web.mapper

import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.web.annotation.PathVariable
import io.zensoft.utils.NumberUtils
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

@Component
class PathVariableMapper: HttpRequestMapper {

    private val pathMatcher = AntPathMatcher()

    override fun supportsAnnotation(annotation: Annotation): Boolean = annotation is PathVariable

    override fun mapParameter(parameter: KParameter, annotation: Annotation): HandlerMethodParameter {
        annotation as PathVariable
        val patternName = if (annotation.value.isEmpty()) parameter.name else annotation.value
        return HandlerMethodParameter(patternName!!, parameter.type.javaType as Class<*>, annotation)
    }

    override fun mapValue(parameter: HandlerMethodParameter, request: FullHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any {
        val pathVariables = pathMatcher.extractUriTemplateVariables(handlerMethod.path, request.uri())
        return NumberUtils.parseNumber(pathVariables[parameter.name]!!, parameter.clazz)
    }

}