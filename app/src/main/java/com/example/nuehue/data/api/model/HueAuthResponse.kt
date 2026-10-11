package com.example.nuehue.data.api.model

data class HueAuthResponse(
    val success: AuthSuccess? = null,
    val error: AuthError? = null
)

data class AuthSuccess(
    val username: String
)

data class AuthError(
    val type: Int,
    val description: String
)
