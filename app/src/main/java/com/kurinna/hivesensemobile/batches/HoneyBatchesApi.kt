package com.kurinna.hivesensemobile.batches

import retrofit2.http.GET

interface HoneyBatchesApi {

    @GET("honey-batches")
    suspend fun getHoneyBatches(): List<HoneyBatchDto>
}