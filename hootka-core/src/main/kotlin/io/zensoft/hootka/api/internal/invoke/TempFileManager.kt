package io.zensoft.hootka.api.internal.invoke

import javax.tools.*

class TempFileManager(
    fileManager: StandardJavaFileManager
): ForwardingJavaFileManager<StandardJavaFileManager>(fileManager) {

    private val output: MutableList<ClassFileObject> = mutableListOf()

    override fun getJavaFileForOutput(location: JavaFileManager.Location, className: String, kind: JavaFileObject.Kind, sibling: FileObject): JavaFileObject {
        val file = ClassFileObject(className, kind)
        output.add(file)
        return file
    }

    fun getOutput(): List<ClassFileObject> = output

}