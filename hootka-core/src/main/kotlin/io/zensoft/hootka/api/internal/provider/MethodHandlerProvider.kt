package io.zensoft.hootka.api.internal.provider

import io.zensoft.hootka.annotation.*
import io.zensoft.hootka.api.exceptions.HandlerMethodNotFoundException
import io.zensoft.hootka.api.internal.support.HandlerMethodKey
import io.zensoft.hootka.api.internal.support.HttpHandlerMetaInfo
import io.zensoft.hootka.api.model.HttpMethod
import io.zensoft.hootka.api.model.HttpStatus
import org.apache.commons.lang3.StringUtils
import org.springframework.context.ApplicationContext
import org.springframework.util.AntPathMatcher
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.*
import javax.annotation.PostConstruct
import kotlin.Comparator
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

class MethodHandlerProvider(
    private val context: ApplicationContext,
    private val handlerParameterMapperProvider: HandlerParameterMapperProvider
) {

    private val antPathMatcher = AntPathMatcher()
    private val storage = TreeMap<HandlerMethodKey, HttpHandlerMetaInfo>(Comparator<HandlerMethodKey> { o1, o2 ->
        val pathComparisonResult = o1.path.compareTo(o2.path)
        if (pathComparisonResult == 0) {
            o1.method.compareTo(o2.method)
        } else {
            pathComparisonResult
        }
    })

    fun getMethodHandler(path: String, httpMethod: HttpMethod): HttpHandlerMetaInfo {
        val stringMethod = httpMethod.toString()
        val handlerMethodKey = HandlerMethodKey(path, httpMethod.toString())
//        return storage.entries
//            .firstOrNull { it.key.method == stringMethod && antPathMatcher.match(it.key.path, path) }?.value
////            .firstOrNull { it.key.path == path && it.key.method == stringMethod }?.value
//            ?: throw HandlerMethodNotFoundException()
        return storage[handlerMethodKey] ?: throw HandlerMethodNotFoundException()
    }

    @PostConstruct
    private fun init() {
        val beans = context.getBeansWithAnnotation(Controller::class.java).values
        for (bean in beans) {
            val superPath = bean::class.findAnnotation<RequestMapping>()?.value?.single() ?: StringUtils.EMPTY
            val globalPrecondition = bean::class.findAnnotation<PreAuthorize>()
            val statelessBeanAnnotation = bean::class.findAnnotation<Stateless>()
            val functions = bean::class.declaredFunctions
            for (function in functions) {
                val pathAnnotation = function.findAnnotation<RequestMapping>() ?: continue
                val parameterMapping = handlerParameterMapperProvider.mapHandlerParameters(function)
                val status = function.findAnnotation<ResponseStatus>()?.value ?: HttpStatus.OK
                val type = pathAnnotation.produces
                val stateless = statelessBeanAnnotation != null || function.findAnnotation<Stateless>() != null
                val preconditionExpression = resolveAllowancePrecondition(globalPrecondition, function)
                val paths = if (pathAnnotation.value.isNotEmpty()) {
                    pathAnnotation.value.map { superPath + it }
                } else {
                    listOf(superPath)
                }
                for (path in paths) {
                    val key = HandlerMethodKey(path, pathAnnotation.method.toString())
                    if (storage.containsKey(key)) {
                        throw IllegalStateException("Mapping $path is already exists.")
                    } else {
                        storage[key] = HttpHandlerMetaInfo(bean, function, parameterMapping,
                            stateless, status, type, path, pathAnnotation.method, preconditionExpression)
                    }
                }
            }
        }
    }

    private fun resolveAllowancePrecondition(globalPrecondition: PreAuthorize?, function: KFunction<*>): String? {
        val methodPrecondition = function.findAnnotation<PreAuthorize>()
        return methodPrecondition?.value ?: globalPrecondition?.value
    }

}