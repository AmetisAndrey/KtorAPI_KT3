
package com.example

import com.example.plugins.configureMonitoring
import com.example.plugins.configureSerialization
import com.example.plugins.configureStatusPages
import com.example.routes.configureRouting
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0"
    ) {
        configureMonitoring()
        configureSerialization()
        configureStatusPages()
        configureRouting()
    }.start(wait = true)
}
