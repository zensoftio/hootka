package io.zensoft.web.mapper

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.QueryStringDecoder
import io.zensoft.web.annotation.ModelAttribute
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import io.zensoft.web.support.HttpMethod
import io.zensoft.web.utils.QueryStringUtils
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import javax.validation.Valid
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

@Component
class ModelAttributeMapper: HttpRequestMapper {

    private val charset = Charset.forName("UTF-8")

    override fun supportsAnnotation(annotation: Annotation): Boolean = annotation is ModelAttribute

    override fun mapValue(parameter: HandlerMethodParameter, request: FullHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any {
        if (HttpMethod.valueOf(request.method().name()) == HttpMethod.POST) {
            val contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE)
            if(contentType == HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()) {
                val params = QueryStringDecoder(request.content().toString(charset), false).parameters()
                return QueryStringUtils.createBeanFromQueryString(parameter.clazz, params)
            }
        }
        throw IllegalArgumentException("Model attribute processes only with post request with form encoded parameters")
    }

    override fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter {
        val annotation = annotations.find { it is ModelAttribute }
        val validationRequired = annotations.find { it is Valid } != null
        return HandlerMethodParameter(parameter.name!!, parameter.type.javaType as Class<*>,
            parameter.type.isMarkedNullable, annotation, validationRequired)
    }

}