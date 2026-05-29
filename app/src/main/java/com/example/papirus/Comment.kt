package com.example.papirus

data class Comment(
    val username: String,
    val text: String,
    val timeAgo: String,
    var likesCount: Int = 0,
    var isLiked: Boolean = false,
    val isReply: Boolean = false
)