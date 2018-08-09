package io.zensoft.web.api.internal.resource

import io.zensoft.web.api.StaticResourceHandler
import java.io.File
import java.io.InputStream

class FilesystemResourceHandler(
    private val mappedPath: String,
    private val basePath: String,
    private val cacheable: Boolean = true
) : StaticResourceHandler {

    override fun getPath(): String = mappedPath

    override fun findResource(url: String): InputStream? {
        val path = "$basePath/$url"
        val file = File(path)
        return if (file.exists()) file.inputStream() else null
    }

    override fun isCacheable(): Boolean = cacheable

}