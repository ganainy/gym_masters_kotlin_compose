package com.ganainy.gymmasterscompose.ui.theme.models


data class Comments(
    val id: String,
    val comments: Map<String, Comment>
)

data class Comment(
    val commentId: String,
    val userId: String,
    val content: CommentContent,
    val timestamp: Long // Timestamp
)

data class CommentContent(
    val text: String,
    val type: String
)