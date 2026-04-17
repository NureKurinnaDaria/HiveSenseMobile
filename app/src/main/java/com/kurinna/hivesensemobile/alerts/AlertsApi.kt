package com.kurinna.hivesensemobile.alerts

import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface AlertsApi {

    @GET("alerts")
    suspend fun getAlerts(): List<AlertDto>

    @PUT("alerts/{id}/acknowledge")
    suspend fun acknowledgeAlert(
        @Path("id") id: Int
    ): AlertDto
}