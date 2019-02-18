package io.zensoft.web.api

import java.io.Serializable

interface HttpSession : Serializable {

    fun getId(): String

    fun getAttributes(): Map<String, Any>

    fun findAttribute(key: String): Any?

    fun <T> findTypedAttribute(key: String, type: Class<T>): T?

    fun setAttribute(key: String, value: Any)

}