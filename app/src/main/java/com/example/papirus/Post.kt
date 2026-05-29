package com.example.papirus

data class Post(
    val postId: String,
    val username: String,
    val userProfileImage: Int,
    val contentText: String,
    val contentMediaResId: Int? = null,
    val timeAgo: String,
    var likesCount: Int,
    var commentsCount: Int,
    var isLiked: Boolean = false // Kalp kırmızı mı gri mi olacak?
)