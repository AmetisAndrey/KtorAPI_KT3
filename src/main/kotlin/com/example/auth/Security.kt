package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

object TokenService {
    private val secret = System.getenv("JWT_SECRET") ?: "local-dev-secret-change-this-before-deploying"
    private val issuer = System.getenv("JWT_ISSUER") ?: "ktor-users-api"
    private val audience = System.getenv("JWT_AUDIENCE") ?: "ktor-users-client"

    fun create(userId: Int, username: String, role: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withClaim("role", role)
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 86_400_000))
            .sign(Algorithm.HMAC256(secret))

    fun verifier() = JWT.require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
}

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(TokenService.verifier())
            validate { credential ->
                if (credential.payload.getClaim("userId").asInt() != null) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(io.ktor.http.HttpStatusCode.Unauthorized, com.example.models.ApiError("UNAUTHORIZED", "Valid bearer token required"))
            }
        }
    }
}

fun JWTPrincipal.userId(): Int = payload.getClaim("userId").asInt()
fun JWTPrincipal.role(): String = payload.getClaim("role").asString() ?: "USER"
fun JWTPrincipal.username(): String = payload.getClaim("username").asString() ?: ""
