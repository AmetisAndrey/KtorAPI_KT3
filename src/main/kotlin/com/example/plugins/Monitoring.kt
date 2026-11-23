
package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO

        format { call ->
            "${call.request.local.method.value} ${call.request.local.uri} -> " +
                    "${call.response.status()?.value ?: "in progress"}"
        }
    }
}
