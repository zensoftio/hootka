package io.zensoft.web.mapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.web.annotation.RequestBody
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import javax.validation.Valid
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

@Component
class RequestBodyMapper: HttpRequestMapper {

    private val jsonMapper = jacksonObjectMapper()
    private val charset = Charset.forName("UTF-8")

    override fun supportsAnnotation(annotation: Annotation): Boolean = annotation is RequestBody

    override fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter {
        val annotation = annotations.find { it is RequestBody }
        val validationRequired = annotations.find { it is Valid } != null
        return HandlerMethodParameter(parameter.name!!, parameter.type.javaType as Class<*>,
            parameter.type.isMarkedNullable, annotation, validationRequired)
    }

    override fun mapValue(parameter: HandlerMethodParameter, request: FullHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any {
        return jsonMapper.readValue(request.content().toString(charset), parameter.clazz)
    }

}