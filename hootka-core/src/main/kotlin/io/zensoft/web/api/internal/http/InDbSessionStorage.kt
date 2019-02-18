package io.zensoft.web.api.internal.http

import io.zensoft.web.api.HttpSession
import io.zensoft.web.api.SessionStorage
import io.zensoft.web.api.WrappedHttpRequest
import io.zensoft.web.api.WrappedHttpResponse
import io.zensoft.web.util.SerializationUtils.deserialize
import io.zensoft.web.util.SerializationUtils.serialize
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class InDbSessionStorage(
    private val jdbcTemplate: JdbcTemplate,
    private val cookieName: String
) : SessionStorage {

    companion object {
        const val INSERT_QUERY = "INSERT INTO session_storage(id,session) VALUES (? , ?)"
        const val SELECT_QUERY = "SELECT session FROM session_storage WHERE id = ?"
        const val DELETE_QUERY = "DELETE FROM session_storage WHERE id = ?"
    }

    override fun findSession(id: String): HttpSession? {
        val result: String = jdbcTemplate.queryForObject(SELECT_QUERY, arrayOf<Any>(id), String::class.java)
        return deserialize(result.toByteArray()) as HttpSession
    }

    override fun createSession(): HttpSession {
        val sessionId = UUID.randomUUID().toString()
        val session = DefaultHttpSession(sessionId)
        jdbcTemplate.update(INSERT_QUERY, sessionId, serialize(session))
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
        cookie?.let { jdbcTemplate.update(DELETE_QUERY, it) }
    }
}