package com.example.papirus.data

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val birthDate: String
)