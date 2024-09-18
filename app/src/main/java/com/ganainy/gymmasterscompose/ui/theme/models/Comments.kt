package com.ganainy.gymmasterscompose.ui.theme.models

data class Comments(
    val id: String = "", // id of the exercise / workout
    val comments: Map<String, Comment> = emptyMap() // Map of user ID to their comment
)

enum class CommentType {
    TEXT,
    GIF,
    PHOTO
}

data class CommentContent(
    val type: CommentType = CommentType.TEXT,
    val text: String? = null,
    val mediaUrl: String? = null
)

data class Comment(
    val commentId: String = "",
    val userId: String = "",
    val content: CommentContent = CommentContent(),
    val timestamp: Long = 0L
)

