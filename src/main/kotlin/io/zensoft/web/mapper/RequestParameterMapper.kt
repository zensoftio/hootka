package io.zensoft.web.mapper

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import io.zensoft.annotation.RequestParam
import io.zensoft.utils.NumberUtils
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import org.springframework.stereotype.Component
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

@Component
class RequestParameterMapper: HttpRequestMapper {

    override fun supportsAnnotation(annotation: Annotation): Boolean = annotation is RequestParam

    override fun mapParameter(parameter: KParameter, annotation: Annotation): HandlerMethodParameter {
        annotation as RequestParam
        return HandlerMethodParameter(parameter.name!!, parameter.type.javaType as Class<*>, annotation)
    }

    override fun mapValue(parameter: HandlerMethodParameter, request: FullHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any {
        val queryParams = QueryStringDecoder(request.uri()).parameters()
        val queryValues = queryParams[parameter.name]
        if(queryValues == null) {
            val annotation = parameter.annotation as RequestParam
            val defaultValue = annotation.defaultValue
            if(defaultValue.isNotEmpty()) {
                return defaultValue
            } else {
                throw IllegalArgumentException("Missing argument with name `${parameter.name}`")
            }
        }
        if (Iterable::class.java.isAssignableFrom(parameter.clazz)) {
            throw IllegalArgumentException("Only array could be passed as query argument")
        }
        if (queryValues.size > 1 && !parameter.clazz.isArray) {
            throw IllegalArgumentException("Expected single argument, but got multiple one")
        }
        if(parameter.clazz.isArray) {
            return queryValues.toTypedArray()
        }
        return NumberUtils.parseNumber(queryValues.first(), parameter.clazz)
    }

}