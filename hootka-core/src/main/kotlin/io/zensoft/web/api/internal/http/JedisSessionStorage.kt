package io.zensoft.web.api.internal.http

import io.zensoft.web.api.HttpSession
import io.zensoft.web.api.SessionStorage
import io.zensoft.web.api.WrappedHttpRequest
import io.zensoft.web.api.WrappedHttpResponse
import io.zensoft.web.util.SerializationUtils.deserialize
import io.zensoft.web.util.SerializationUtils.serialize
import org.springframework.stereotype.Component
import redis.clients.jedis.Jedis
import java.util.*
import javax.annotation.PostConstruct

@Component
class JedisSessionStorage(
    private val cookieName: String,
    private val jedis: Jedis
) : SessionStorage {

    override fun findSession(id: String): HttpSession
        = deserialize(jedis.get(id).toByteArray()) as HttpSession

    override fun createSession(): HttpSession {
        val sessionId = UUID.randomUUID().toString()
        val session = DefaultHttpSession(sessionId)
        jedis.set(sessionId, String(serialize(session)))
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
            jedis.del(it)
        }
    }

    @PostConstruct
    private fun init() {
        jedis.auth("")
    }

}