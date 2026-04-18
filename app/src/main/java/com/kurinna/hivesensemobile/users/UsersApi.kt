package com.kurinna.hivesensemobile.users

import retrofit2.http.GET
import com.kurinna.hivesensemobile.network.SaveFcmTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface UsersApi {

    @GET("users/me")
    suspend fun getCurrentUser(): UserDto

    @POST("users/me/fcm-token")
    suspend fun saveMyFcmToken(
        @Body request: SaveFcmTokenRequest
    )
}