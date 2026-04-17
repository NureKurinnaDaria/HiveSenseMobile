package com.kurinna.hivesensemobile.network

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val access_token: String,
    val user: LoginUserDto
)

data class LoginUserDto(
    val id: Int,
    val email: String,
    val role: String
)