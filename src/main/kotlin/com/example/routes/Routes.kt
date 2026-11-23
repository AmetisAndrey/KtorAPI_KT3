package com.example.routes

import com.example.auth.*
import com.example.models.*
import com.example.plugins.ConflictException
import com.example.plugins.NotFoundException
import com.example.repository.UserRepository
import com.example.websocket.NotificationHub
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt

private val users = UserRepository()

fun Application.configureRoutes() {
    routing {
        get("/") { call.respond(mapOf("name" to "Ktor Users API", "docs" to "/swagger", "openapi" to "/openapi.yaml")) }
        get("/api/health") { call.respond(HealthResponse("ok")) }

        post("/api/auth/register") {
            val body = call.receive<RegisterRequest>()
            validateCredentials(body.username, body.password)
            if (users.findByUsername(body.username) != null) throw ConflictException("Username is already taken")
            val user = users.create(body.username, BCrypt.hashpw(body.password, BCrypt.gensalt()), "USER")
            call.respond(HttpStatusCode.Created, AuthResponse(TokenService.create(user.id, user.username, user.role), user.username, user.role))
        }

        post("/api/auth/login") {
            val body = call.receive<LoginRequest>()
            val user = users.findByUsername(body.username)
            if (user == null || !BCrypt.checkpw(body.password, user.passwordHash)) {
                call.respond(HttpStatusCode.Unauthorized, ApiError("INVALID_CREDENTIALS", "Username or password is incorrect"))
            } else {
                call.respond(AuthResponse(TokenService.create(user.id, user.username, user.role), user.username, user.role))
            }
        }

        authenticate("auth-jwt") {
            get("/api/users/me") {
                val principal = call.principal<JWTPrincipal>()!!
                val user = users.findById(principal.userId()) ?: throw NotFoundException("User not found")
                call.respond(user.toDto())
            }

            put("/api/users/me") {
                val principal = call.principal<JWTPrincipal>()!!
                val body = call.receive<UpdateProfileRequest>()
                val username = body.username?.trim()?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("username must not be blank")
                if (users.findByUsername(username) != null) throw ConflictException("Username is already taken")
                val updated = users.updateUsername(principal.userId(), username) ?: throw NotFoundException("User not found")
                call.respond(updated.toDto())
                NotificationHub.broadcast(NotificationEvent("USER_UPDATED", updated.id, updated.username))
            }

            get("/api/users") {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.role() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ApiError("FORBIDDEN", "Admin role required"))
                } else call.respond(users.list())
            }

            delete("/api/users/{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                if (principal.role() != "ADMIN") {
                    call.respond(HttpStatusCode.Forbidden, ApiError("FORBIDDEN", "Admin role required"))
                } else {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("id must be an integer")
                    val target = users.findById(id) ?: throw NotFoundException("User not found")
                    users.delete(id)
                    call.respond(DeleteResponse(deleted = true, id = id))
                    NotificationHub.broadcast(NotificationEvent("USER_DELETED", target.id, target.username))
                }
            }
        }
    }
}

private fun validateCredentials(username: String, password: String) {
    require(username.trim().length in 3..80) { "username must contain 3-80 characters" }
    require(password.length >= 8) { "password must contain at least 8 characters" }
}
