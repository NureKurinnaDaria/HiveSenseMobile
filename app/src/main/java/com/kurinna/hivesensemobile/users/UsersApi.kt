package com.kurinna.hivesensemobile.users

import retrofit2.http.GET

interface UsersApi {

    @GET("users/me")
    suspend fun getCurrentUser(): UserDto
}