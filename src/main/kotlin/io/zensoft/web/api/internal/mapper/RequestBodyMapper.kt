package io.zensoft.web.api.internal.mapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.zensoft.web.annotation.RequestBody
import io.zensoft.web.api.HttpRequestMapper
import io.zensoft.web.api.WrappedHttpRequest
import io.zensoft.web.api.internal.support.HandlerMethodParameter
import io.zensoft.web.api.internal.support.HttpHandlerMetaInfo
import org.springframework.stereotype.Component
import javax.validation.Valid
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

@Component
class RequestBodyMapper: HttpRequestMapper {

    private val jsonMapper = jacksonObjectMapper()

    override fun supportsAnnotation(annotations: List<Annotation>): Boolean {
        return annotations.find { it is RequestBody } != null
    }

    override fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter {
        val annotation = annotations.find { it is RequestBody }
        val validationRequired = annotations.find { it is Valid } != null
        return HandlerMethodParameter(parameter.name!!, parameter.type.javaType as Class<*>,
            parameter.type.isMarkedNullable, annotation, validationRequired)
    }

    override fun createValue(parameter: HandlerMethodParameter, request: WrappedHttpRequest<*>, handlerMethod: HttpHandlerMetaInfo): Any {
        val iStream = request.getContentStream()
        return jsonMapper.readValue(iStream, parameter.clazz)
    }

}