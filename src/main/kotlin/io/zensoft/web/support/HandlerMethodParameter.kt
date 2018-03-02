package io.zensoft.web.support

class HandlerMethodParameter(
    val name: String,
    val clazz: Class<*>,
    val annotation: Annotation? = null,
    val validationRequired: Boolean = false
)