package com.kurinna.hivesensemobile.users

data class UserDto(
    val user_id: Int,
    val email: String,
    val full_name: String,
    val role: String,
    val is_active: Boolean,
    val warehouse_id: Int?,
    val warehouse: UserWarehouseDto?
)

data class UserWarehouseDto(
    val warehouse_id: Int,
    val name: String,
    val location: String,
    val status: String
)