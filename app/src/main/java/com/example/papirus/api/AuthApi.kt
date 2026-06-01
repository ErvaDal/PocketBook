package com.example.papirus

import com.example.papirus.data.AllBooksResponse
import com.example.papirus.data.ApiResponse
import com.example.papirus.data.BookDetailsResponse
import com.example.papirus.data.ForgotPasswordRequest
import com.example.papirus.data.LoginRequest
import com.example.papirus.data.RegisterRequest
import com.example.papirus.data.ProfileResponse
import com.example.papirus.data.CreateBookRequest
import com.example.papirus.data.LikeRequest
import com.example.papirus.data.SingleChapterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApi {

    @GET("test-db")
    fun testDb(): Call<ApiResponse>

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<ApiResponse>


    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse>


    @POST("auth/forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<ApiResponse>

    @GET("api/users/profile/{userId}")
    fun getUserProfile(@Path("userId") userId: String): Call<ProfileResponse>

    @POST("/api/stories/create-with-chapter")
    fun createBookWithChapter(@Body request: CreateBookRequest): Call<ApiResponse>

    @POST("api/stories/like-chapter")
    fun likeChapter(@Body request: LikeRequest): Call<ApiResponse>
    @GET("api/stories/all")
    fun getAllStories(): Call<AllBooksResponse>

    @GET("api/stories/details/{storyId}")
    fun getBookDetails(@Path("storyId") storyId: String): Call<BookDetailsResponse>

    @GET("api/stories/chapter/{storyId}/{chapterNumber}")
    fun getChapterContent(
        @Path("storyId") storyId: String,
        @Path("chapterNumber") chapterNumber: Int
    ): Call<SingleChapterResponse>

    @GET("api/stories/search/{query}")
    fun searchStories(@Path("query") query: String): Call<AllBooksResponse>

    @PUT("api/stories/update-status/{storyId}")
    fun updateBookStatus(@Path("storyId") storyId: String, @Body body: Map<String, String>): Call<ApiResponse>

    @PUT("api/stories/update-details/{storyId}")
    fun updateBookDetails(@Path("storyId") storyId: String, @Body body: Map<String, String>): Call<ApiResponse>

    @PUT("api/stories/delete-story/{storyId}")
    fun deleteStory(@Path("storyId") storyId: String): Call<ApiResponse>
}