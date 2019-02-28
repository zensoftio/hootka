package io.zensoft.hootka.api.internal.mapper

import io.zensoft.hootka.annotation.Principal
import io.zensoft.hootka.api.HttpRequestMapper
import io.zensoft.hootka.api.SecurityProvider
import io.zensoft.hootka.api.internal.support.HandlerMethodParameter
import io.zensoft.hootka.api.internal.support.HttpHandlerMetaInfo
import io.zensoft.hootka.api.internal.support.RequestContext
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaType

class PrincipalMapper(
    private val securityProvider: SecurityProvider<*>
) : HttpRequestMapper {

    override fun getSupportedAnnotation(): KClass<out Annotation> = Principal::class

    override fun supportsAnnotation(annotations: List<Annotation>): Boolean {
        return null != annotations.find { it is Principal }
    }

    override fun mapParameter(parameter: KParameter, annotations: List<Annotation>): HandlerMethodParameter {
        val annotation = annotations.find { it is Principal }
        return HandlerMethodParameter(parameter.name!!, parameter.type.javaType as Class<*>,
            parameter.type.isMarkedNullable, annotation)
    }

    override fun createValue(parameter: HandlerMethodParameter, context: RequestContext, handlerMethod: HttpHandlerMetaInfo): Any? {
        return securityProvider.findPrincipal(context)
    }

}