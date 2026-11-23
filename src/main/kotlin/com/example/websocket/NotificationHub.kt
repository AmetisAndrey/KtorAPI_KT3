package com.example.websocket

import com.example.models.NotificationEvent
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

object NotificationHub {
    private val sessions = Collections.newSetFromMap(ConcurrentHashMap<DefaultWebSocketServerSession, Boolean>())

    suspend fun broadcast(event: NotificationEvent) {
        val message = Frame.Text(Json.encodeToString(event))
        sessions.toList().forEach { session ->
            runCatching { session.send(message) }.onFailure { sessions.remove(session) }
        }
    }

    fun add(session: DefaultWebSocketServerSession) { sessions.add(session) }
    fun remove(session: DefaultWebSocketServerSession) { sessions.remove(session) }
}

fun Application.configureSockets() {
    install(WebSockets)
    routing {
        webSocket("/ws/notifications") {
            NotificationHub.add(this)
            try {
                send(Frame.Text("""{"type":"CONNECTED","message":"Listening for user events"}"""))
                for (frame in incoming) {
                    if (frame is Frame.Close) break
                }
            } finally {
                NotificationHub.remove(this)
            }
        }
    }
}
