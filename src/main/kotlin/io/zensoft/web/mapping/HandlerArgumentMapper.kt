package io.zensoft.web.mapping

interface HandlerArgumentMapper<in T: Annotation> {

    fun createArgumentMetaInfo(annotation: T)

    fun createArgument(content: String, clazz: Class<*>)

}