package com.example.papirus.model;

import com.example.papirus.data.ApiResponse
import com.example.papirus.data.ForgotPasswordRequest
import com.example.papirus.data.LoginRequest
import com.example.papirus.data.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("test-db")
    fun testDb(): Call<ApiResponse>

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<ApiResponse>

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse>

    @POST("auth/forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<ApiResponse>
}
