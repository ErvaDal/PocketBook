package com.example.papirus.data

data class CreateBookRequest(
    val authorId: String,
    val title: String,
    val summary: String,
    val coverImageUrl: String?,
    val chapterTitle: String,
    val chapterContent: String
)