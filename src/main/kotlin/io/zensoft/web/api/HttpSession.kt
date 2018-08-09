package io.zensoft.web.api

interface HttpSession {

    fun getId(): String

    fun getAttributes(): Map<String, Any>

    fun findAttribute(key: String): Any?

    fun <T> findTypedAttribute(key: String, type: Class<T>): T?

    fun setAttribute(key: String, value: Any)

}