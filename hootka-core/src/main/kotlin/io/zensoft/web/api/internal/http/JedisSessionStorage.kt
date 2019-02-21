package io.zensoft.web.api.internal.http

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin
import io.zensoft.web.api.HttpSession
import io.zensoft.web.api.SessionStorage
import io.zensoft.web.api.WrappedHttpRequest
import io.zensoft.web.api.WrappedHttpResponse
import io.zensoft.web.util.SerializationUtils.deserialize
import io.zensoft.web.util.SerializationUtils.serialize
import org.springframework.stereotype.Component
import redis.clients.jedis.Jedis
import java.util.*

@Component
class JedisSessionStorage(
    private val cookieName: String,
    private val cookieExpiry: Long,
    private val jedis: Jedis
) : SessionStorage {

    private var timestamp: Long = 0


    override fun findSession(id: String): HttpSession? {
        if (!isRelevant()) {
            jedis.del(id)
            return null
        }
        return deserialize(HexBin.decode(jedis.get(id))) as HttpSession
    }

    override fun createSession(): HttpSession {
        val sessionId = UUID.randomUUID().toString()
        val session = DefaultHttpSession(sessionId)
        timestamp = System.currentTimeMillis()

        jedis.set(sessionId, HexBin.encode(serialize(session)))
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

    private fun isRelevant(): Boolean = ((System.currentTimeMillis() - timestamp) / 1000) <= cookieExpiry

}