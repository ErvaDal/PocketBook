package com.example.papirus

import com.google.gson.annotations.SerializedName

// 🚀 TERTEMİZ VE DOĞRU MODEL:
data class Book(
    @SerializedName("StoryId") val storyId: String, // Aradığımız anahtar tam olarak bu!
    @SerializedName("Title") val title: String,
    @SerializedName("CoverImageResId") val coverImageResId: Int,
    @SerializedName("IsDraft") val isDraft: Boolean
)