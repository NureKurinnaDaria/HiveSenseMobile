package com.kurinna.hivesensemobile.measurements

data class MeasurementDto(
    val measurement_id: Int,
    val measured_at: String,
    val temperature_c: String,
    val humidity_percent: String,
    val sensor_id: Int,
    val sensor: MeasurementSensorDto
)

data class MeasurementSensorDto(
    val sensor_id: Int,
    val serial_number: String,
    val type: String,
    val is_active: Boolean,
    val warehouse_id: Int
)