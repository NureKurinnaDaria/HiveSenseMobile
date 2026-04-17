package com.kurinna.hivesensemobile.alerts

data class AlertDto(
    val alert_id: Int,
    val type: String,
    val status: String,
    val created_at: String,
    val resolved_at: String?,
    val warehouse_id: Int,
    val sensor_id: Int?,
    val user_id: Int?
)