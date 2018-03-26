package io.zensoft.config

import io.zensoft.web.support.HandlerMethodKey
import org.junit.Test
import javax.el.ExpressionFactory
import javax.el.StandardELContext

class ElEngineTest {

    @Test
    fun testEl() {
        val f = ExpressionFactory.newInstance()
        val key = HandlerMethodKey("yo, motherfuckers", "POST")
        val context = StandardELContext(f)
        val variable = f.createValueExpression(key, HandlerMethodKey::class.java)
        context.variableMapper.setVariable("key", variable)
        val result = f.createValueExpression(context, "\${key.getStrInfo('salt')}", String::class.java)
        println(result.getValue(context))
    }

}