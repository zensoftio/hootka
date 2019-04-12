package io.zensoft.hootka.api.internal.invoke

import java.net.URI
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject

class StringJavaFileObject(
    className: String,
    private val source: String
): SimpleJavaFileObject(
    URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension),
    JavaFileObject.Kind.SOURCE
) {

    override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
        return source
    }

}