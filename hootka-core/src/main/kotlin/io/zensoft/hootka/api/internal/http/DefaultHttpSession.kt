package io.zensoft.hootka.api.internal.http

import io.zensoft.hootka.api.HttpSession

class DefaultHttpSession(
    private val id: String,
    private val attributes: MutableMap<String, Any> = HashMap()
) : HttpSession{

    override fun getId(): String {
        return id
    }

    override fun getAttributes(): Map<String, Any> {
        return HashMap(attributes)
    }

    override fun findAttribute(key: String): Any? {
        return attributes[key]
    }

    override fun <T> findTypedAttribute(key: String, type: Class<T>): T? {
        val attribute = attributes[key]
        return attribute?.let { type.cast(attribute) }
    }

    override fun setAttribute(key: String, value: Any) {
        attributes[key] = value
    }

}