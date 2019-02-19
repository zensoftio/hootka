package io.zensoft.web.api.internal.mapper

import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.QueryStringDecoder
import io.zensoft.web.annotation.ModelAttribute
import io.zensoft.web.api.HttpRequestMapper
import io.zensoft.web.api.internal.support.HandlerMethodParameter
import io.zensoft.web.api.internal.support.HttpHandlerMetaInfo
import io.zensoft.web.api.internal.support.RequestContext
import io.zensoft.web.api.internal.utils.DeserializationUtils
import io.zensoft.web.api.model.HttpMethod
import java.nio.charset.Charset
import javax.validation.Valid
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

class ModelAttributeMapper : HttpRequestMapper {

    override fun supportsAnnotation(annotations: List<Annotation>): Boolean {
        return annotations.find { it is ModelAttribute } != null
    }

    override fun createValue(parameter: HandlerMethodParameter, context: RequestContext, handlerMethod: HttpHandlerMetaInfo): Any {
        if (context.request.getMethod() == HttpMethod.POST) {
            val contentType = context.request.getHeader("Content-Type")
            if (contentType == HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()) {
                val queryParams = QueryStringDecoder(context.request.getContentAsString(Charset.defaultCharset()), false).parameters()
                return DeserializationUtils.createBeanFromQueryString(parameter.clazz, queryParams)
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