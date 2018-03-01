package io.zensoft.web.mapper

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.QueryStringDecoder
import io.zensoft.web.annotation.RequestParam
import io.zensoft.utils.NumberUtils
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import io.zensoft.web.support.HttpMethod
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

@Component
class RequestParameterMapper: HttpRequestMapper {

    private val charset = Charset.forName("UTF-8")

    override fun supportsAnnotation(annotation: Annotation): Boolean = annotation is RequestParam

    override fun mapParameter(parameter: KParameter, annotation: Annotation): HandlerMethodParameter {
        annotation as RequestParam
        return HandlerMethodParameter(parameter.name!!, parameter.type.javaType as Class<*>, annotation)
    }

    override fun mapValue(parameter: HandlerMethodParameter, request: FullHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any {
        val queryParams = if (HttpMethod.POST == HttpMethod.valueOf(request.method().name())) {
            val contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE)
            if (contentType != HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()) {
                throw IllegalArgumentException("Cannot map request parameter. Mismatched content type for post request")
            }
            QueryStringDecoder(request.content().toString(charset), false).parameters()
        } else {
            QueryStringDecoder(request.uri()).parameters()
        }
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