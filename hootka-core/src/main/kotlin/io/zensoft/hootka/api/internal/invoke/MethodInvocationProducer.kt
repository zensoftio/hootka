package io.zensoft.hootka.api.internal.invoke

import io.zensoft.hootka.api.internal.support.HandlerMethodParameter
import java.util.*
import javax.tools.ToolProvider
import kotlin.reflect.KFunction

class MethodInvocationProducer {

    companion object {
        private val classNameTemplate = "io.zensoft.hootka.generated.%s"

        private val sourceTemplate = """
            package io.zensoft.hootka.generated;

            import io.zensoft.hootka.api.internal.invoke.MethodInvocation;
            %s

            public class %s implements MethodInvocation {

                private %s bean;

                public %s(%s bean) {
                    this.bean = bean;
                }

                @Override
                public Object invoke(Object[] args) {
                    %s
                }

            }
        """
    }

    private val compiler = ToolProvider.getSystemJavaCompiler()

    fun generateMethodInvocation(bean: Any, function: KFunction<*>, parameters: List<HandlerMethodParameter>): MethodInvocation {
        val className = function.name.capitalize()
        val fullClassName = String.format(classNameTemplate, className)
        val source = generateSource(bean, function, parameters)

        val unit = StringJavaFileObject(fullClassName, source)
        val fileManager = TempFileManager(compiler.getStandardFileManager(null, null, null))

        val compilationTask = compiler.getTask(null, fileManager, null, null, null,
            Arrays.asList(unit))
        compilationTask.call()

        val classLoader = MethodInvocationsClassLoader(fileManager)
        val clazz = classLoader.loadClass(fullClassName)
        return clazz.constructors[0].newInstance(bean) as MethodInvocation
    }

    private fun generateSource(bean: Any, function: KFunction<*>, parameters: List<HandlerMethodParameter>): String {
        val className = function.name.capitalize()
        val beanType = bean::class.java.simpleName
        val imports = generateImports(bean, parameters)
        val implementation = implement(function, parameters)
        return String.format(sourceTemplate, imports, className, beanType, className, beanType, implementation)
    }

    private fun generateImports(bean: Any, parameters: List<HandlerMethodParameter>): String {
        val imports = mutableSetOf("import ${bean::class.java.name};")
        for (param in parameters) {
            imports.add("import ${param.clazz.name};")
        }
        return imports.joinToString("\n")
    }

    private fun implement(function: KFunction<*>, parameters: List<HandlerMethodParameter>): String {
        val result = mutableListOf<String>()
        parameters.forEachIndexed { idx, it ->
            val className = it.clazz.simpleName
            result.add("$className arg$idx = ($className)args[$idx];")
        }
        val args = mutableListOf<String>()
        for (i in 0 until result.size) {
            args.add("arg$i")
        }
        result.add("return bean.${function.name}(${args.joinToString()});")
        return result.joinToString("\n")
    }

}