package io.zensoft.web.api

import java.io.File

interface StaticResourceHandler {

    fun findResource(url: String): File?

}