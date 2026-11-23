
package com.example.plugins

import com.example.models.ApiError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

class UserNotFoundException(id: Int) :
    RuntimeException("User with id=$id was not found")

fun Application.configureStatusPages() {
    install(StatusPages) {

        exception<UserNotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiError(
                    "USER_NOT_FOUND",
                    cause.message ?: "User not found"
                )
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError(
                    "BAD_REQUEST",
                    cause.message ?: "Invalid request"
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.application.environment.log.error(
                "Unhandled server error",
                cause
            )

            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(
                    "INTERNAL_SERVER_ERROR",
                    "An unexpected error occurred"
                )
            )
        }
    }
}
