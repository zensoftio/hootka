package io.zensoft.web.session

import java.time.Instant

class Session(
    val id: String,
    var lastActive: Instant = Instant.now(),
    private val attributes: MutableMap<String, Any> = mutableMapOf()
) {

    fun getAttribute(key: String): Any? = attributes[key]

    fun setAttribute(key: String, value: Any): Any? = attributes.put(key, value)

    fun attributeNames(): Set<String> = attributes.keys

}