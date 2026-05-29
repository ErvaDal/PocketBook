package com.example.papirus

// Veritabanındaki 'Stories' tablosunun karşılığı olan model sınıfımız
data class Book(
    val storyId: String,
    val title: String,
    val coverImageResId: Int,
    val isDraft: Boolean = false // Profilde taslakları ayırmak için eklendi (Eski kodları bozmaz)
)