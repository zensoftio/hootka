package io.zensoft.web.api.internal.security

import io.zensoft.web.api.*
import io.zensoft.web.api.exceptions.AuthenticationFailedException
import io.zensoft.web.api.internal.support.RequestContext
import io.zensoft.web.api.model.SimpleAuthenticationDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class DefaultSecurityProvider(
    private val sessionStorage: SessionStorage,
    private val userDetailsProvider: UserDetailsService,
    private val rememberMeService: RememberMeService? = null
) : SecurityProvider<SimpleAuthenticationDetails> {

    companion object {
        private const val PRINCIPAL_SESSION_ATTRIBUTE = "SEC_PRINCIPAL"
    }

    private val encoder = BCryptPasswordEncoder()

    override fun authenticate(authenticationDetails: SimpleAuthenticationDetails): UserDetails {
        val user = userDetailsProvider.findUserDetailsByUsername(authenticationDetails.username)
        if (user == null || !user.isEnabled() || !encoder.matches(authenticationDetails.password, user.getPassword())) {
            throw AuthenticationFailedException("Invalid username or password.")
        }
        this.invalidate(authenticationDetails.request, authenticationDetails.response)
        val session = sessionStorage.createAndAssignSession(authenticationDetails.response)
        session.setAttribute(PRINCIPAL_SESSION_ATTRIBUTE, user)
        if (authenticationDetails.rememberMe) {
            this.rememberMe(user, authenticationDetails.response)
        }
        return user
    }

    override fun findPrincipal(context: RequestContext): UserDetails? {
        if (null != context.session) {
            return context.session!!.findAttribute(PRINCIPAL_SESSION_ATTRIBUTE) as? UserDetails
        }
        val session = sessionStorage.resolveSession(context.request) ?: return null
        return session.findAttribute(PRINCIPAL_SESSION_ATTRIBUTE) as? UserDetails
    }

    override fun invalidate(request: WrappedHttpRequest, response: WrappedHttpResponse?) {
        if (null != response) {
            rememberMeService?.invalidateToken(request, response)
        }
        sessionStorage.removeSession(request)
    }

    override fun encodePassword(plainPassword: String): String = encoder.encode(plainPassword)

    override fun rememberMe(userDetails: UserDetails, response: WrappedHttpResponse) {
        rememberMeService?.createToken(userDetails, response)
    }

    override fun remindMe(requestContext: RequestContext): UserDetails? {
        val user = rememberMeService?.performAutoAuthentication(requestContext) ?: return null
        this.invalidate(requestContext.request)
        val session = sessionStorage.createAndAssignSession(requestContext.response)
        session.setAttribute(PRINCIPAL_SESSION_ATTRIBUTE, user)
        requestContext.session = session
        return user
    }

}