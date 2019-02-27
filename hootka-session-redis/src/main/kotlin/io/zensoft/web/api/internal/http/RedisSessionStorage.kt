package io.zensoft.web.api.internal.http

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin
import io.zensoft.web.api.HttpSession
import io.zensoft.web.api.SessionStorage
import io.zensoft.web.api.WrappedHttpRequest
import io.zensoft.web.api.WrappedHttpResponse
import io.zensoft.web.util.SerializationUtils.deserialize
import io.zensoft.web.util.SerializationUtils.serialize
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit


@Component
class RedisSessionStorage(
    private val cookieName: String,
    private val cookieExpiry: Long,
    private val redisTemplate: StringRedisTemplate
) : SessionStorage {

    override fun findSession(id: String): HttpSession? {
        val session = redisTemplate.opsForValue().get(id)
        if (session != null) {
            redisTemplate.expire(id, cookieExpiry, TimeUnit.SECONDS)
            return deserialize(HexBin.decode(session)) as HttpSession
        }
        return null
    }

    override fun createSession(): HttpSession {
        val sessionId = UUID.randomUUID().toString()
        val session = DefaultHttpSession(sessionId)

        redisTemplate.opsForValue().set(sessionId, HexBin.encode(serialize(session)), cookieExpiry, TimeUnit.SECONDS)
        return session
    }

    override fun createAndAssignSession(response: WrappedHttpResponse): HttpSession {
        val session = this.createSession()
        response.setCookie(cookieName, session.getId(), true, null)
        return session
    }

    override fun resolveSession(request: WrappedHttpRequest): HttpSession? {
        val cookie = request.getCookies()[cookieName]
        return cookie?.let { findSession(it) }
    }

    override fun removeSession(request: WrappedHttpRequest) {
        val cookie = request.getCookies()[cookieName]
        cookie?.let {
            redisTemplate.delete(it)
        }
    }

}