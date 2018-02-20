package io.zensoft.web

import io.netty.handler.codec.http.FullHttpRequest
import io.zensoft.annotation.HttpBody
import io.zensoft.annotation.RequestMapping
import io.zensoft.web.support.HttpHandlerMetaInfo
import io.zensoft.web.support.HttpMethod
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import java.util.*
import javax.annotation.PostConstruct
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaType

@Component
class PathHandlerProvider(private val context: ApplicationContext) {

    private val storage = TreeMap<String, HttpHandlerMetaInfo>()

    @PostConstruct
    private fun init() {
        val beans = context.getBeansWithAnnotation(Controller::class.java).values
        for (bean in beans) {
            val functions = bean::class.declaredFunctions

            for (function in functions) {
                val pathAnnotation = function.findAnnotation<RequestMapping>() ?: continue
                val path = pathAnnotation.value

                val parameters = function.parameters
                val entityType = parameters.find { it.findAnnotation<HttpBody>() != null }?.
                        type?.javaType as? Class<*>

                if (storage.containsKey(pathAnnotation.value)) {
                    throw IllegalStateException("Mapping $path is already exists.")
                } else {
                    storage[path] = HttpHandlerMetaInfo(bean, function, path, pathAnnotation.method, entityType)
                }
            }
        }
    }

    fun getHandler(request: FullHttpRequest) : HttpHandlerMetaInfo? {
        val handler = storage[request.uri()] ?: return null
        if(handler.httpMethod != HttpMethod.valueOf(request.method().name())) return null
        return handler
    }

}