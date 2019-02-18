package io.zensoft.web.api.internal.support

class HandlerMethodParameter(
    val name: String,
    val clazz: Class<*>,
    val nullable: Boolean = false,
    val annotation: Annotation? = null,
    val validationRequired: Boolean = false
)