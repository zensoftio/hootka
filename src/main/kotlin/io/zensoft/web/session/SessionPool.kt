package io.zensoft.web.session

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class SessionPool(
    private @Value("\${server.session.cookie.max-age}") val sessionTimeout: Long
): Runnable {

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val sessions: MutableMap<String, Session> = Collections.synchronizedMap(LinkedHashMap(16, 0.75f, true))

    override fun run() {
        val instant = Instant.now().epochSecond
        sessions.entries.reversed()
            .takeWhile { instant - it.value.lastActive.epochSecond > sessionTimeout }
            .forEach { sessions.remove(it.key) }
        val scheduleFromInstant = if (sessions.isNotEmpty()) sessions.values.last().lastActive else Instant.now()
        executor.schedule(this, scheduleFromInstant.plusSeconds(sessionTimeout).epochSecond - instant, TimeUnit.SECONDS)
    }

    fun createSession(id: String): Session {
        val session = Session(id)
        sessions[id] = session
        return session
    }

    fun getSession(id: String): Session? {
        val session = sessions[id]
        session!!.lastActive = Instant.now()
        return session
    }

    @PostConstruct
    private fun init() {
        executor.schedule(this, sessionTimeout, TimeUnit.SECONDS)
    }

}