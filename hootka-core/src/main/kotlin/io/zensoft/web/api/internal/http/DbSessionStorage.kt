package io.zensoft.web.api.internal.http

import io.zensoft.web.api.HttpSession
import io.zensoft.web.api.SessionStorage
import io.zensoft.web.api.WrappedHttpRequest
import io.zensoft.web.api.WrappedHttpResponse
import io.zensoft.web.util.SerializationUtils.deserialize
import io.zensoft.web.util.SerializationUtils.serialize
import org.springframework.stereotype.Component
import java.sql.PreparedStatement
import java.util.*
import javax.annotation.PostConstruct
import javax.sql.DataSource


@Component
class DbSessionStorage(
    private val dataSource: DataSource,
    private val cookieName: String
) : SessionStorage {

    companion object {
        const val INSERT_QUERY = "INSERT INTO session_storage(id,session) VALUES (? , ?)"
        const val SELECT_QUERY = "SELECT session FROM session_storage WHERE id = ?"
        const val DELETE_QUERY = "DELETE FROM session_storage WHERE id = ?"
        const val CREATE_SESSION_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS session_storage" +
            "(" +
            "  id      VARCHAR PRIMARY KEY HASH," +
            "  session VARCHAR NOT NULL" +
            ");"
    }

    override fun findSession(id: String): HttpSession? {
        val st = getPreparedStatement(SELECT_QUERY)
        st.setString(1, id)
        val result = st.executeQuery()

        return deserialize(result.getString("session").toByteArray()) as HttpSession
    }

    override fun createSession(): HttpSession {
        val sessionId = UUID.randomUUID().toString()
        val session = DefaultHttpSession(sessionId)

        val st = getPreparedStatement(INSERT_QUERY)
        st.setString(1, sessionId)
        st.setString(2, String(serialize(session)))
        st.executeUpdate()

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
            val st = getPreparedStatement(DELETE_QUERY)
            st.setString(1, it)
            st.executeUpdate()
        }
    }

    fun getPreparedStatement(query: String): PreparedStatement {
        val conn = dataSource.connection
        return conn.prepareStatement(query)
    }

    @PostConstruct
    private fun init() {
        val st = getPreparedStatement(CREATE_SESSION_TABLE_QUERY)
        st.executeUpdate()
        st.close()
    }

}