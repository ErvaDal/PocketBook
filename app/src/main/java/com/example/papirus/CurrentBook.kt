package com.example.papirus

// Hem 'Stories' hem de 'ReadLogs' verilerini tutan model sınıfımız
data class CurrentBook(
    val storyId: String,
    val title: String,
    val coverImageResId: Int,
    val readProgress: Int
)