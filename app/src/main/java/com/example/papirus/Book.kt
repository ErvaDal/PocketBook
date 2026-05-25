package com.example.papirus

// Veritabanındaki 'Stories' tablosunun karşılığı olan model sınıfımız
data class Book(
    val storyId: String,
    val title: String,
    val coverImageResId: Int
)