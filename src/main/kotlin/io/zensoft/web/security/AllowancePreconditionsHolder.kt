package io.zensoft.web.security

import io.zensoft.web.annotation.AllowancePreconditions
import io.zensoft.web.exception.PreconditionNotSatisfiedException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.el.ExpressionFactory
import javax.el.StandardELContext
import kotlin.reflect.full.findAnnotation

@Component
class AllowancePreconditionsHolder(
    private val applicationContext: ApplicationContext
) {

    private val expressionFactory = ExpressionFactory.newInstance()
    private val context = StandardELContext(expressionFactory)

    fun checkAllowance(expression: String) {
        val valueExpression = expressionFactory.createValueExpression(context, expression, Boolean::class.java)
        val result = valueExpression.getValue(context) as Boolean
        if (!result) {
            throw PreconditionNotSatisfiedException()
        }
    }

    @PostConstruct
    private fun init() {
        val list = applicationContext.getBeansWithAnnotation(AllowancePreconditions::class.java).values
        for (precondition in list) {
            val preconditionName = precondition::class.findAnnotation<AllowancePreconditions>()!!.name
            val variable = expressionFactory.createValueExpression(precondition, precondition::class.java)
            context.variableMapper.setVariable(preconditionName, variable)
        }
    }

}