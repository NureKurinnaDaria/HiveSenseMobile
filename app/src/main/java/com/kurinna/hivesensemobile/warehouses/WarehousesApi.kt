package com.kurinna.hivesensemobile.warehouses

import retrofit2.http.GET

interface WarehousesApi {

    @GET("warehouses")
    suspend fun getWarehouses(): List<WarehouseDto>
}