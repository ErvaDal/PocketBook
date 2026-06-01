package com.example.papirus.data

data class LikeRequest(
    val storyId: String,
    val chapterNumber: Int,
    val likerUsername: String
)