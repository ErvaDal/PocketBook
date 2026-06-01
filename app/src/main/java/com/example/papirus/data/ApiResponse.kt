package com.example.papirus.data

data class ApiResponse(
    val success: Boolean,
    val message: String?,
    val user: UserData?
)

data class UserData(
    val id: String,
    val username: String,
    val email: String
)