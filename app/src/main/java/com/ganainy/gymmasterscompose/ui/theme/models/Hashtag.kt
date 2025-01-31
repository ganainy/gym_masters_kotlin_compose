package com.ganainy.gymmasterscompose.ui.theme.models

/**
 * Data class representing a hashtag.
 *
 * @property tag The text of the hashtag.
 * @property postCount The number of posts associated with the hashtag.
 * @property workoutCount The number of workouts associated with the hashtag.
 * @property lastUsed The timestamp when the hashtag was last used.
 */
data class Hashtag(
    val tag: String = "",
    val postCount: Int = 0,
    val workoutCount: Int = 0,
    val lastUsed: Long = 0
)