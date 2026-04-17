package com.kurinna.hivesensemobile.network

import com.kurinna.hivesensemobile.alerts.AlertsApi
import com.kurinna.hivesensemobile.core.SessionHolder
import com.kurinna.hivesensemobile.measurements.MeasurementsApi
import com.kurinna.hivesensemobile.users.UsersApi
import com.kurinna.hivesensemobile.warehouses.WarehousesApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.kurinna.hivesensemobile.batches.HoneyBatchesApi

object ApiClient {

    private const val BASE_URL = "https://hivesense.onrender.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = SessionHolder.accessToken

        val newRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(newRequest)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val warehousesApi: WarehousesApi = retrofit.create(WarehousesApi::class.java)
    val usersApi: UsersApi = retrofit.create(UsersApi::class.java)
    val measurementsApi: MeasurementsApi = retrofit.create(MeasurementsApi::class.java)
    val alertsApi: AlertsApi = retrofit.create(AlertsApi::class.java)
    val honeyBatchesApi: HoneyBatchesApi = retrofit.create(HoneyBatchesApi::class.java)
}