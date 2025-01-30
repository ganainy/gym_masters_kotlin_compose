package com.ganainy.gymmasterscompose.ui.theme.models


data class FeedPost(
    val id: String = "",
    val authorId: String = "",
    val content: String = "",
    val imagePathList: List<String> = emptyList(),
    val imageUrlList: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val linkedExerciseId: String? = null,
    val linkedWorkoutId: String? = null,
    val hashtags: List<String> = emptyList(),
)


data class PostStats(
    val postId: String = "",
    val likes: Int = 0,
    val comments: Int = 0,
    val shares: Int = 0
)
