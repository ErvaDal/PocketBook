package com.example.papirus.data

import com.example.papirus.Book
import com.example.papirus.UserLibrary
import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("user") val user: UserDataDetails,
    @SerializedName("publishedBooks") val publishedBooks: List<DBBook>,
    @SerializedName("publicLibraries") val publicLibraries: List<UserLibrary>
)

data class UserDataDetails(
    @SerializedName("Id") val id: String,
    @SerializedName("Username") val username: String,
    @SerializedName("Email") val email: String,
    @SerializedName("Bio") val bio: String?
)// Kitap Detay API'sinden dönecek paket
data class BookDetailsResponse(
    val success: Boolean,
    val story: DBBook,
    val chapters: List<DBChapter>
)

// Ana sayfadaki tüm kitapları listelerken dönecek paket
data class AllBooksResponse(
    val success: Boolean,
    val books: List<DBBook>
)

// Tek bir bölüm içeriği için dönecek paket
data class SingleChapterResponse(
    val success: Boolean,
    val chapter: DBChapter
)

//kitap modeli
data class DBBook(
    @SerializedName("StoryID") val storyId: String,
    @SerializedName("AuthorID") val authorId: String,
    @SerializedName("Title") val title: String,
    @SerializedName("Summary") val summary: String,
    @SerializedName("CoverImageURL") val coverImageUrl: String?,
    @SerializedName("AuthorName") val authorName: String?
)

//böşüm modeli
data class DBChapter(
    @SerializedName("ChapterID") val chapterId: String,
    @SerializedName("StoryID") val storyId: String,
    @SerializedName("ChapterNumber") val chapterNumber: Int,
    @SerializedName("Content") val content: String?,
    @SerializedName("StarCount") val starCount: Int,
    @SerializedName("CommentCount") val commentCount: Int
)
