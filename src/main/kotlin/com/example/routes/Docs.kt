package com.example.routes

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.response.respondText

fun Application.configureDocs() {
    routing {
        get("/openapi.yaml") {
            val content = this::class.java.classLoader.getResource("openapi.yaml")?.readText()
                ?: error("openapi.yaml resource missing")
            call.respondText(content, io.ktor.http.ContentType.parse("application/yaml"))
        }
        get("/swagger") {
            call.respondText(
                """
                <!doctype html><html><head><title>Ktor Users API - Swagger</title>
                <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css"></head>
                <body><div id="swagger-ui"></div>
                <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
                <script>SwaggerUIBundle({url:'/openapi.yaml',dom_id:'#swagger-ui',persistAuthorization:true});</script>
                </body></html>
                """.trimIndent(),
                io.ktor.http.ContentType.Text.Html
            )
        }
    }
}
