package io.zensoft.web.api.internal.resource

import io.zensoft.web.api.StaticResourceHandler
import java.io.File

class ClasspathResourceHandler(
    private val basePath: String
): StaticResourceHandler {

    override fun findResource(url: String): File? {
        val path = basePath + url
        val uri = this::class.java.classLoader.getResource(path).toURI()
        return if(uri != null) File(uri) else null
    }

}