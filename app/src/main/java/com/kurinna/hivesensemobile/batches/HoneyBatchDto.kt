package com.kurinna.hivesensemobile.batches

data class HoneyBatchDto(
    val batch_id: Int,
    val variety: String,
    val quantity_kg: String,
    val received_date: String,
    val expiration_date: String?,
    val status: String,
    val warehouse_id: Int
)