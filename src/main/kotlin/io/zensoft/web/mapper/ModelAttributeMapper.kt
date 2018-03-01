package io.zensoft.web.mapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import io.zensoft.web.annotation.ModelAttribute
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

@Component
class ModelAttributeMapper: HttpRequestMapper {

    private val charset = Charset.forName("UTF-8")
    private val mapper = jacksonObjectMapper()

    override fun supportsAnnotation(annotation: Annotation): Boolean = annotation is ModelAttribute

    override fun mapValue(parameter: HandlerMethodParameter, request: FullHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any {
        val params = QueryStringDecoder(request.content().toString(charset), false).parameters()
        return mapper.convertValue(params, parameter.clazz)
    }

    override fun mapParameter(parameter: KParameter, annotation: Annotation): HandlerMethodParameter {
        annotation as ModelAttribute
        return HandlerMethodParameter(parameter.name!!, parameter.type.javaType as Class<*>, annotation)
    }

}