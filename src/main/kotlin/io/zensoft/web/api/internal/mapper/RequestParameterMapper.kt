package io.zensoft.web.api.internal.mapper

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.QueryStringDecoder
import io.zensoft.web.annotation.RequestParam
import io.zensoft.web.api.model.HttpMethod
import io.zensoft.web.api.HttpRequestMapper
import io.zensoft.web.api.WrappedHttpRequest
import io.zensoft.web.api.internal.support.HandlerMethodParameter
import io.zensoft.web.api.internal.support.HttpHandlerMetaInfo
import io.zensoft.web.api.internal.utils.NumberUtils
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

@Component
class RequestParameterMapper: HttpRequestMapper {

    private val charset = Charset.forName("UTF-8")

    override fun supportsAnnotation(annotations: List<Annotation>): Boolean {
        return annotations.find { it is RequestParam } != null
    }

    override fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter {
        val annotation = annotations.find { it is RequestParam } as RequestParam
        val name = if (annotation.value.isEmpty()) parameter.name else annotation.value
        return HandlerMethodParameter(name!!, parameter.type.javaType as Class<*>,
            parameter.type.isMarkedNullable, annotation)
    }

    override fun createValue(parameter: HandlerMethodParameter, request: WrappedHttpRequest<*>, handlerMethod: HttpHandlerMetaInfo): Any? {
        val queryParams = if (HttpMethod.POST == request.getMethod()) {
            val contentType = request.getHeaders()[HttpHeaderNames.CONTENT_TYPE.toString()]
            if (contentType != HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()) {
                throw IllegalArgumentException("Cannot map request parameter. Mismatched content type for post request")
            }
            QueryStringDecoder(request.getContentAsString(charset), false).parameters()
        } else {
            request.getQueryParameters()
        }
        val queryValues = queryParams[parameter.name] ?: if (parameter.nullable) {
            return null
        } else {
            throw IllegalArgumentException("Missing required query argument named ${parameter.name}")
        }
        if (queryValues.size > 1) {
            if(parameter.clazz.isArray) {
                return queryValues.toTypedArray()
            } else {
                throw IllegalArgumentException("Expected single argument, but got multiple one")
            }
        }
        return NumberUtils.parseNumber(queryValues.first(), parameter.clazz)
    }

}