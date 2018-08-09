package io.zensoft.web.api

import java.io.InputStream

interface StaticResourceHandler {

    fun getPath(): String

    fun findResource(url: String): InputStream?

    fun isCacheable(): Boolean

}