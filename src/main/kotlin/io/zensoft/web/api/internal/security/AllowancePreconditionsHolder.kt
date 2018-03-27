package io.zensoft.web.api.internal.security

import io.zensoft.web.annotation.AllowancePreconditions
import io.zensoft.web.api.HttpSession
import io.zensoft.web.api.SessionStorage
import io.zensoft.web.api.WrappedHttpRequest
import io.zensoft.web.api.exceptions.PreconditionNotSatisfiedException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.el.ELContext
import javax.el.ExpressionFactory
import javax.el.StandardELContext
import kotlin.reflect.full.findAnnotation

@Component
class AllowancePreconditionsHolder(
    private val applicationContext: ApplicationContext,
    private val sessionStorage: SessionStorage
) {

    private val expressionFactory = ExpressionFactory.newInstance()
    private val preconditionsHolderMap = HashMap<String, Any>()

    companion object {
        private const val SESSION_PLACEHOLDER = "session"
        private const val REQUEST_PLACEHOLDER = "request"
    }

    fun checkAllowance(expression: String, request: WrappedHttpRequest<*>) {
        val elContext = createElContext(expression, request)
        val elExpression = "\${$expression}"
        val valueExpression = expressionFactory.createValueExpression(elContext, elExpression, Boolean::class.java)
        val result = valueExpression.getValue(elContext) as Boolean
        if (!result) throw PreconditionNotSatisfiedException("Precondition evaluated with value `false`")
    }

    private fun createElContext(expression: String, request: WrappedHttpRequest<*>): ELContext {
        val preconditionName = expression.substring(0, expression.indexOf('.'))
        val precondition = preconditionsHolderMap[preconditionName]
            ?: PreconditionNotSatisfiedException("Precondition `$preconditionName` not found")
        val elContext = StandardELContext(expressionFactory)
        val preconditionVar = expressionFactory.createValueExpression(precondition, precondition::class.java)
        elContext.variableMapper.setVariable(preconditionName, preconditionVar)
        if (expression.contains(SESSION_PLACEHOLDER)) {
            val session = sessionStorage.resolveSession(request)
                ?: throw PreconditionNotSatisfiedException("Session not found, while accessing path ${request.getPath()}")
            val sessionVar = expressionFactory.createValueExpression(session, HttpSession::class.java)
            elContext.variableMapper.setVariable(SESSION_PLACEHOLDER, sessionVar)
        }
        if (expression.contains(REQUEST_PLACEHOLDER)) {
            val requestVar = expressionFactory.createValueExpression(request, WrappedHttpRequest::class.java)
            elContext.variableMapper.setVariable(REQUEST_PLACEHOLDER, requestVar)
        }
        return elContext
    }

    @PostConstruct
    private fun init() {
        val list = applicationContext.getBeansWithAnnotation(AllowancePreconditions::class.java).values
        for (precondition in list) {
            val preconditionName = precondition::class.findAnnotation<AllowancePreconditions>()!!.name
            preconditionsHolderMap[preconditionName] = precondition
        }
    }

}