package io.zensoft.web.support

import kotlin.reflect.KFunction

class HttpHandlerMetaInfo(
    private val instance: Any,
    private val handlerMethod: KFunction<*>,
    val parameters: Map<String, HandlerMethodParameter>,
    val status: HttpStatus = HttpStatus.OK,
    val contentType: MimeType = MimeType.APPLICATION_JSON,
    val path: String = "",
    val httpMethod: HttpMethod = HttpMethod.GET
) {

    fun execute(vararg args: Any): Any? = handlerMethod.call(instance, *args)

}