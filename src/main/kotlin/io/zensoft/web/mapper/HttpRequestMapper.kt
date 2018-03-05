package io.zensoft.web.mapper

import io.zensoft.web.support.HandlerMethodParameter
import io.zensoft.web.support.HttpHandlerMetaInfo
import io.zensoft.web.support.WrappedHttpRequest
import kotlin.reflect.KParameter

interface HttpRequestMapper {

    fun supportsAnnotation(annotations: List<Annotation>): Boolean

    fun createValue(parameter: HandlerMethodParameter, request: WrappedHttpRequest, handlerMethod: HttpHandlerMetaInfo): Any?

    fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter

}