package io.zensoft.web.provider

import io.zensoft.web.annotation.*
import io.zensoft.web.support.HttpHandlerMetaInfo
import io.zensoft.web.support.HttpStatus
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.util.AntPathMatcher
import javax.annotation.PostConstruct
import kotlin.collections.HashMap
import kotlin.reflect.full.*

@Component
class PathHandlerProvider(
        private val context: ApplicationContext,
        private val handlerParameterMapperProvider: HandlerParameterMapperProvider
) {

    private val antPathMatcher = AntPathMatcher()
    private val storage = HashMap<String, HttpHandlerMetaInfo>()

    fun getMethodHandler(path: String) : HttpHandlerMetaInfo? {
        return storage.keys
                .firstOrNull { antPathMatcher.match(it, path) }
                ?.let { storage[it] }
    }

    @PostConstruct
    private fun init() {
        val beans = context.getBeansWithAnnotation(Controller::class.java).values
        for (bean in beans) {
            val superPathAnnotation = bean::class.findAnnotation<RequestMapping>()
            val superPath = superPathAnnotation?.value ?: ""
            val functions = bean::class.declaredFunctions
            val statelessBeanAnnotation = bean::class.findAnnotation<Stateless>()
            for (function in functions) {
                val pathAnnotation = function.findAnnotation<RequestMapping>() ?: continue
                val path = superPath + pathAnnotation.value
                val parameterMapping = handlerParameterMapperProvider.getHandlerParameters(function)
                val status = function.findAnnotation<ResponseStatus>()?.value ?: HttpStatus.OK
                val type = pathAnnotation.produces
                val stateless = statelessBeanAnnotation != null || function.findAnnotation<Stateless>() != null
                if (storage.containsKey(pathAnnotation.value)) {
                    throw IllegalStateException("Mapping $path is already exists.")
                } else {
                    storage[path] = HttpHandlerMetaInfo(bean, function, parameterMapping,
                        stateless, status, type, path, pathAnnotation.method)
                }
            }
        }
    }

}