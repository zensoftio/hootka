package io.zensoft.web.mapper

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.QueryStringDecoder
import io.zensoft.web.annotation.RequestParam
import io.zensoft.web.utils.NumberUtils
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import io.zensoft.web.support.HttpMethod
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

@Component
class RequestParameterMapper : HttpRequestMapper {

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

    override fun createValue(parameter: HandlerMethodParameter, request: FullHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any? {
        val queryParams = if (HttpMethod.POST == HttpMethod.valueOf(request.method().name())) {
            val contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE)
            if (contentType != HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()) {
                throw IllegalArgumentException("Cannot map request parameter. Mismatched content type for post request")
            }
            QueryStringDecoder(request.content().toString(charset), false).parameters()
        } else {
            QueryStringDecoder(request.uri()).parameters()
        }
        val queryValues = queryParams[parameter.name] ?: if (parameter.nullable) {
            return null
        } else {
            throw IllegalArgumentException("Missing required query argument named ${parameter.name}")
        }
        if (Iterable::class.java.isAssignableFrom(parameter.clazz)) {
            throw IllegalArgumentException("Only array could be passed as query argument")
        }
        if (queryValues.size > 1 && !parameter.clazz.isArray) {
            throw IllegalArgumentException("Expected single argument, but got multiple one")
        }
        if (parameter.clazz.isArray) {
            return queryValues.toTypedArray()
        }
        return NumberUtils.parseNumber(queryValues.first(), parameter.clazz)
    }

}