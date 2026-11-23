package com.example.routes
import com.example.models.CreateUserRequest
import com.example.repository.UserRepository
import com.example.plugins.UserNotFoundException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val repository = UserRepository()
    routing {
        get("/") { call.respondText("Ktor Users API is running") }
        get("/api/health") { call.respond(mapOf("status" to "ok")) }

        route("/api/users") {
            get { call.respond(repository.findAll()) }
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Path parameter 'id' must be an integer")
                call.respond(repository.findById(id) ?: throw UserNotFoundException(id))
            }
            post {
                val request = call.receive<CreateUserRequest>()
                if (request.name.isBlank()) throw IllegalArgumentException("Name must not be blank")
                if (!request.email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))) {
                    throw IllegalArgumentException("A valid email address is required")
                }
                val user = repository.create(request)
                call.response.headers.append(HttpHeaders.Location, "/api/users/${user.id}")
                call.respond(HttpStatusCode.Created, user)
            }
            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Path parameter 'id' must be an integer")
                if (!repository.delete(id)) throw UserNotFoundException(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }

        get("/swagger") {
            call.respondText(
                """
                <!doctype html>
                <html lang="en"><head><meta charset="utf-8"/>
                <title>Ktor Users API - Swagger UI</title>
                <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css"/>
                </head><body><div id="swagger-ui"></div>
                <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
                <script>window.onload = () => SwaggerUIBundle({url:"/openapi.yaml",dom_id:"#swagger-ui"});</script>
                </body></html>
                """.trimIndent(), ContentType.Text.Html
            )
        }
        get("/openapi.yaml") {
            val resource = this::class.java.classLoader.getResource("openapi.yaml")
                ?: error("openapi.yaml resource is missing")
            call.respondText(resource.readText(), ContentType.parse("application/yaml"))
        }
    }
}
