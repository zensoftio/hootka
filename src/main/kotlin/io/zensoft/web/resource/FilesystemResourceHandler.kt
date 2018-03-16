package io.zensoft.web.resource

import java.io.File

class FilesystemResourceHandler(
    private val basePath: String
): ResourceHandler {

    override fun findResource(url: String): File? {
        val path = basePath + url
        val file = File(path)
        return if(file.exists()) file else null
    }

}