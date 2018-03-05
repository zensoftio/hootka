package io.zensoft.web.mapper

import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import io.zensoft.web.support.WrappedHttpRequest
import io.zensoft.web.utils.DeserializationUtils
import org.springframework.stereotype.Component
import javax.validation.Valid
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

@Component
class ValidMapper: HttpRequestMapper {

    override fun supportsAnnotation(annotations: List<Annotation>): Boolean {
        return annotations.size == 1 && annotations.find { it is Valid } != null
    }

    override fun createValue(parameter: HandlerMethodParameter, request: WrappedHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any? {
        return DeserializationUtils.createBeanFromQueryString(parameter.clazz, request.queryParams)
    }

    override fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter {
        val annotation = annotations.find { it is Valid }
        return HandlerMethodParameter(parameter.name!!, parameter.type.javaType as Class<*>,
            parameter.type.isMarkedNullable, annotation, true)
    }


}