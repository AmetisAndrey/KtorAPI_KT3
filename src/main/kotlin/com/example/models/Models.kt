package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val username: String, val password: String)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val username: String, val role: String)

@Serializable
data class UserDto(val id: Int, val username: String, val role: String)

@Serializable
data class UpdateProfileRequest(val username: String? = null)

@Serializable
data class ApiError(val code: String, val message: String)

@Serializable
data class HealthResponse(val status: String)

@Serializable
data class DeleteResponse(val deleted: Boolean, val id: Int)

@Serializable
data class NotificationEvent(val type: String, val userId: Int, val username: String)
