package com.ganainy.gymmasterscompose.ui.theme.models

data class Rating(
    val ratingId: String = "",
    val raterId: String = "",
    val ratedUserId: String = "",
    val score: Double = 0.0,
    val timestamp: Long = 0L,
    val comment: String? = null,
)