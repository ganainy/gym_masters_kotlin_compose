package com.ganainy.gymmasterscompose.ui.theme.models

import User

data class FeedPost(
    val id: String = "",
    val authorId: String = "",
    val content: String = "",
    val mediaUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val linkedExerciseId: String? = null,
    val linkedWorkoutId: String? = null,
    val hashtags: List<String> = emptyList(),
    var author: User? = null,
    var linkedExercise: Exercise? = null,
    var linkedWorkout: Workout? = null,
    var stats: PostStats = PostStats(),
    var currentUserReaction: String? = null
)



data class PostStats(
    val likes: Int = 0,
    val comments: Int = 0,
    val shares: Int = 0
)
