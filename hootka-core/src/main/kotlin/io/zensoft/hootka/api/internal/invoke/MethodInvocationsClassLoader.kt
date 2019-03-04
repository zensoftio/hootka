package io.zensoft.hootka.api.internal.invoke

class MethodInvocationsClassLoader(
    fileManager: TempFileManager,
    private val classObjects: List<ClassFileObject> = fileManager.getOutput()
): ClassLoader() {

    override fun findClass(name: String): Class<*> {
        val classObject = classObjects.find { name == it.getClassName() }
        classObject?.let {
            val bytes = it.getBytes()
            return super.defineClass(name, bytes, 0, bytes.size)
        }
        return super.findClass(name)
    }

}