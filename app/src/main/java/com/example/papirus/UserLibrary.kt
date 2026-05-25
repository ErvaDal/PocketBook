package com.example.papirus

data class UserLibrary(
    val libraryId: String,
    val libraryName: String,
    val bookCount: Int,
    var isPublic: Boolean
)