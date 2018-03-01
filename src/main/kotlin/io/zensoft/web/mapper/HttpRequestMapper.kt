package io.zensoft.web.mapper

import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import kotlin.reflect.KParameter

interface HttpRequestMapper {

    fun supportsAnnotation(annotation: Annotation): Boolean

    fun mapValue(parameter: HandlerMethodParameter, request: FullHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any

    fun mapParameter(parameter: KParameter, annotation: Annotation): HandlerMethodParameter

}