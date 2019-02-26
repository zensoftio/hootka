package io.zensoft.hootka.api.internal.support

import io.zensoft.hootka.api.model.HttpMethod
import io.zensoft.hootka.api.model.HttpStatus
import io.zensoft.hootka.api.model.MimeType
import kotlin.reflect.KFunction

class HttpHandlerMetaInfo(
    private val instance: Any,
    private val handlerMethod: KFunction<*>,
    val parameters: List<HandlerMethodParameter>,
    val stateless: Boolean = false,
    val status: HttpStatus = HttpStatus.OK,
    val contentType: MimeType = MimeType.APPLICATION_JSON,
    val path: String = "",
    val httpMethod: HttpMethod = HttpMethod.GET,
    val preconditionExpression: String? = null
) {

    fun execute(vararg args: Any?): Any? = handlerMethod.call(instance, *args)

}