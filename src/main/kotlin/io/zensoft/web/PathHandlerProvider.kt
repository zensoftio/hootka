package io.zensoft.web

import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.annotation.HttpBody
import io.zensoft.annotation.PathVariable
import io.zensoft.annotation.RequestMapping
import io.zensoft.web.support.HttpHandlerMetaInfo
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.util.AntPathMatcher
import java.util.*
import javax.annotation.PostConstruct
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaType

@Component
class PathHandlerProvider(
        private val context: ApplicationContext
) {

    private val antPathMatcher = AntPathMatcher()
    private val storage = TreeMap<String, HttpHandlerMetaInfo>()

    @PostConstruct
    private fun init() {
        val beans = context.getBeansWithAnnotation(Controller::class.java).values
        for (bean in beans) {
            val superPathAnnotation = bean::class.findAnnotation<RequestMapping>()
            val superPath = superPathAnnotation?.value ?: ""
            val functions = bean::class.declaredFunctions

            for (function in functions) {
                val pathAnnotation = function.findAnnotation<RequestMapping>() ?: continue
                val path = superPath + pathAnnotation.value

                val parameters = function.parameters
                val pathVariables = parameters.filter { it.findAnnotation<PathVariable>() != null }
                val pathVariableMapping = linkedMapOf<String, Class<*>>()
                for (pathVariable in pathVariables) {
                    val annotation = pathVariable.findAnnotation<PathVariable>()!!
                    val patternName = if (annotation.value.isEmpty()) pathVariable.name else annotation.value
                    pathVariableMapping.put(patternName!!, pathVariable.type.javaType as Class<*>)
                }

                val entityType = parameters.find { it.findAnnotation<HttpBody>() != null }?.
                        type?.javaType as? Class<*>

                if (storage.containsKey(pathAnnotation.value)) {
                    throw IllegalStateException("Mapping $path is already exists.")
                } else {
                    storage[path] = HttpHandlerMetaInfo(bean, function, path, pathVariableMapping, pathAnnotation.method, entityType)
                }
            }
        }
    }

    fun getHandler(request: FullHttpRequest) : HttpHandlerMetaInfo? {
        return storage.keys
                .firstOrNull { antPathMatcher.match(it, request.uri()) }
                ?.let { storage[it] }
    }

}