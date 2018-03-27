package io.zensoft.web.api

import io.zensoft.web.api.internal.support.HandlerMethodParameter
import io.zensoft.web.api.internal.support.HttpHandlerMetaInfo
import kotlin.reflect.KParameter

interface HttpRequestMapper {

    fun supportsAnnotation(annotations: List<Annotation>): Boolean

    fun createValue(parameter: HandlerMethodParameter, request: WrappedHttpRequest<*>, handlerMethod: HttpHandlerMetaInfo): Any?

    fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter

}