package io.zensoft.hootka.api.internal.invoke

interface MethodInvocation {

    fun invoke(args: Array<Any?>): Any

}