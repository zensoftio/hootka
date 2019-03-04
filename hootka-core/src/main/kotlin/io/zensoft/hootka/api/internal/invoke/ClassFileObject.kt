package io.zensoft.hootka.api.internal.invoke

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.URI
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject

class ClassFileObject(
    private val className: String,
    kind: JavaFileObject.Kind,
    private val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()
): SimpleJavaFileObject(
    URI.create("mem:///" + className.replace('.', '/') + kind.extension),
    kind
) {

    override fun openOutputStream(): OutputStream = outputStream

    fun getBytes(): ByteArray = outputStream.toByteArray()

    fun getClassName(): String = className

}