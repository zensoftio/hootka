package io.zensoft.web.api.internal.security

import io.zensoft.web.api.*
import io.zensoft.web.api.exceptions.InvalidRememberMeTokenException
import io.zensoft.web.api.internal.support.RequestContext
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.security.crypto.codec.Hex
import java.security.MessageDigest
import java.util.*

class DefaultRememberMeService(
    private val cookieName: String,
    private val cookieMaxAge: Long,
    private val salt: String,
    private val userDetailsService: UserDetailsService
): RememberMeService {

    companion object {
        private const val TOKEN_PARTS_QUANTITY = 3
    }

    override fun createToken(userDetails: UserDetails, response: WrappedHttpResponse): String {
        val expirationTime = System.currentTimeMillis() + (1000 * cookieMaxAge)
        val hexedSignature = createSignature(userDetails, expirationTime.toString())
        val rememberMeToken = "${userDetails.getUsername()}:$expirationTime:$hexedSignature"
        val token = String(Base64.getEncoder().encode(rememberMeToken.toByteArray()))
        response.setCookie(cookieName, token, true, cookieMaxAge)
        return token
    }

    override fun invalidateToken(request: WrappedHttpRequest, response: WrappedHttpResponse) {
        val rememberMeCookie = request.getCookies()[cookieName]
        if (null != rememberMeCookie) {
            response.setCookie(cookieName, RandomStringUtils.randomAlphanumeric(30), true, null)
        }
    }

    override fun performAutoAuthentication(requestContext: RequestContext): UserDetails? {
        val token = requestContext.request.getCookies()[cookieName] ?: return null
        val decodedToken = String(Base64.getDecoder().decode(token))
        val tokens = decodedToken.split(":")
        if (TOKEN_PARTS_QUANTITY != tokens.size) {
            throw InvalidRememberMeTokenException("Token consists of more than 3 parts")
        }
        val expirationTime = tokens[1].toLong()
        if (expirationTime < System.currentTimeMillis()) {
            throw InvalidRememberMeTokenException("Token is expired")
        }
        val user = userDetailsService.findUserDetailsByUsername(tokens[0])
            ?: throw InvalidRememberMeTokenException("User details not found by token")

        val signature = createSignature(user, tokens[1])
        if (signature != tokens[2]) {
            throw InvalidRememberMeTokenException("Invalid token signature")
        }
        return user
    }

    private fun createSignature(userDetails: UserDetails, expirationTime: String): String {
        val messageDigest = MessageDigest.getInstance("MD5")
        val signature = "${userDetails.getUsername()}:$expirationTime:${userDetails.getPassword()}:$salt".toByteArray()
        return String(Hex.encode(messageDigest.digest(signature)))
    }

}