package io.zensoft.web.support

data class HandlerMethodKey(
    val path: String,
    val method: String
) {
    fun getStrInfo(salt: String): String {
        return "$salt $path $method"
    }
}