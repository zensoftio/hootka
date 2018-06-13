package io.zensoft.web.api

import io.zensoft.web.api.internal.support.HandlerMethodParameter
import io.zensoft.web.api.internal.support.HttpHandlerMetaInfo
import io.zensoft.web.api.internal.support.RequestContext
import kotlin.reflect.KParameter

interface HttpRequestMapper {

    fun supportsAnnotation(annotations: List<Annotation>): Boolean

    fun createValue(parameter: HandlerMethodParameter, context: RequestContext, handlerMethod: HttpHandlerMetaInfo): Any?

    fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter

}