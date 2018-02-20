package io.zensoft.web.support

import kotlin.reflect.KFunction

class HttpHandlerMetaInfo(
        private val instance: Any,
        private val handlerMethod: KFunction<*>,
        val path: String,
        val httpMethod: HttpMethod,
        val entityType: Class<*>?
) {

    fun execute(vararg args: Any): Any? = handlerMethod.call(instance, *args)

}