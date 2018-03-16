package io.zensoft.web.resource

import java.io.File

interface ResourceHandler {

    fun findResource(url: String): File?

}