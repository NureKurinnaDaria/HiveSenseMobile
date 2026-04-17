package com.kurinna.hivesensemobile.measurements

import retrofit2.http.GET

interface MeasurementsApi {

    @GET("measurements")
    suspend fun getMeasurements(): List<MeasurementDto>
}