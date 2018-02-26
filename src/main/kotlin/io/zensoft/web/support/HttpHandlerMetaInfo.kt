package io.zensoft.web.support

import kotlin.reflect.KFunction

class HttpHandlerMetaInfo(
        private val instance: Any,
        private val handlerMethod: KFunction<*>,
        val path: String,
        val pathVariables: Map<String, HandlerMethodParameter>,
        val httpMethod: HttpMethod
) {

    fun execute(vararg args: Any): Any? = handlerMethod.call(instance, *args)

}