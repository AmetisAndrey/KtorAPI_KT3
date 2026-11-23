package com.example

import com.example.auth.configureSecurity
import com.example.database.DatabaseFactory
import com.example.plugins.configureMonitoring
import com.example.plugins.configureSerialization
import com.example.plugins.configureStatusPages
import com.example.routes.configureRoutes
import com.example.routes.configureDocs
import com.example.websocket.NotificationHub
import com.example.websocket.configureSockets
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    DatabaseFactory.init()
    embeddedServer(Netty, port = System.getenv("PORT")?.toIntOrNull() ?: 8080, host = "0.0.0.0") {
        configureMonitoring()
        configureSerialization()
        configureStatusPages()
        configureSecurity()
        configureSockets()
        configureRoutes()
        configureDocs()
    }.start(wait = true)
}
