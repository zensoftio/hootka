package io.zensoft.web.support

class Session(
    val id: String,
    var isNew: Boolean = true,
    private val attributes: MutableMap<String, Any> = mutableMapOf()
) {

    fun getAttribute(key: String): Any? = attributes[key]

    fun setAttribute(key: String, value: Any): Any? = attributes.put(key, value)

    fun attributeNames(): Set<String> = attributes.keys

}