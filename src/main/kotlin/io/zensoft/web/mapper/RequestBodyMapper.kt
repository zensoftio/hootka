package io.zensoft.web.mapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.buffer.ByteBufInputStream
import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.web.annotation.RequestBody
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import org.springframework.stereotype.Component
import java.io.InputStream
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

    override fun createValue(parameter: HandlerMethodParameter, request: FullHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any {
        val iStream = ByteBufInputStream(request.content())
        return jsonMapper.readValue(iStream as InputStream, parameter.clazz)
    }

}