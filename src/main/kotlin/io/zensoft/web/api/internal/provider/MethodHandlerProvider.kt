package io.zensoft.web.api.internal.provider

import io.zensoft.web.annotation.*
import io.zensoft.web.api.model.HttpMethod
import io.zensoft.web.api.model.HttpStatus
import io.zensoft.web.api.exceptions.HandlerMethodNotFoundException
import io.zensoft.web.api.internal.support.HandlerMethodKey
import io.zensoft.web.api.internal.support.HttpHandlerMetaInfo
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import javax.annotation.PostConstruct
import kotlin.collections.HashMap
import kotlin.reflect.full.*

@Component
class MethodHandlerProvider(
        private val context: ApplicationContext,
        private val handlerParameterMapperProvider: HandlerParameterMapperProvider
) {

    private val antPathMatcher = AntPathMatcher()
    private val storage = HashMap<HandlerMethodKey, HttpHandlerMetaInfo>()

    fun getMethodHandler(path: String, httpMethod: HttpMethod) : HttpHandlerMetaInfo {
        val stringMethod = httpMethod.toString()
        return storage.entries
                .firstOrNull { it.key.method == stringMethod && antPathMatcher.match(it.key.path, path) }?.value
                ?: throw HandlerMethodNotFoundException()
    }

    @PostConstruct
    private fun init() {
        val beans = context.getBeansWithAnnotation(Controller::class.java).values
        for (bean in beans) {
            val superPath = bean::class.findAnnotation<RequestMapping>()?.value ?: ""
            val statelessBeanAnnotation = bean::class.findAnnotation<Stateless>()
            val functions = bean::class.declaredFunctions
            for (function in functions) {
                val pathAnnotation = function.findAnnotation<RequestMapping>() ?: continue
                val path = superPath + pathAnnotation.value
                val parameterMapping = handlerParameterMapperProvider.mapHandlerParameters(function)
                val status = function.findAnnotation<ResponseStatus>()?.value ?: HttpStatus.OK
                val type = pathAnnotation.produces
                val stateless = statelessBeanAnnotation != null || function.findAnnotation<Stateless>() != null
                val preconditionExpression = function.findAnnotation<AllowInCase>()?.value
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