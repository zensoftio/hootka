package io.zensoft.hootka.api.internal.response

import io.zensoft.hootka.api.HttpResponseResolver
import io.zensoft.hootka.api.WrappedHttpResponse
import io.zensoft.hootka.api.model.InMemoryFile
import io.zensoft.hootka.api.model.MimeType
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream

class FileResponseResolver : HttpResponseResolver {

    override fun getContentType(): MimeType = MimeType.APPLICATION_OCTET_STREAM

    override fun resolveResponseBody(result: Any, handlerArgs: Array<Any?>, response: WrappedHttpResponse): ByteArray {
        val output = when (result) {
            is InMemoryFile -> {
                Pair(result.name, IOUtils.toByteArray(result.stream))
            }
            is File -> {
                val byteArray = ByteArray(result.length().toInt())
                val fis = FileInputStream(result)
                fis.use { it.read(byteArray) }
                Pair(result.name, byteArray)
            }
            else -> throw IllegalStateException("Wrong return type for method, which produces file")
        }

        response.setHeader("Content-Disposition", "attachment; filename=\"${output.first}\"")
        return output.second
    }

}