package io.zensoft.web

import java.util.*

class Session(
        val id: String,
        private val attributes: MutableMap<String, Any>
) {

    fun getAttribute(key: String): Any? = attributes[key]

    fun setAttribute(key: String, value: Any): Any? = attributes.put(key, value)

    fun attributeNames(): Set<String> = attributes.keys

}