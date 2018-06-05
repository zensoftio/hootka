package io.zensoft.web.api.model

import java.util.HashMap

class ViewModel {
    private val attributes: MutableMap<String, Any> = HashMap()

    fun setAttribute(key: String, value: Any) {
        attributes[key] = value
    }

    fun getAttribute(key: String): Any? = attributes[key]

    fun getAttributes(): Map<String, Any> = HashMap(attributes)

}