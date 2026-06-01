package com.example.papirus
import com.google.gson.annotations.SerializedName

data class UserLibrary(
    @SerializedName("LibraryId") val libraryId: String,
    @SerializedName("LibraryName") val libraryName: String,
    @SerializedName("BookCount") val bookCount: Int,
    @SerializedName("IsPublic") var isPublic: Boolean
)