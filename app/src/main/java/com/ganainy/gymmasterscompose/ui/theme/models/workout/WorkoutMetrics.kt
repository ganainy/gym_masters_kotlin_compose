package com.ganainy.gymmasterscompose.ui.theme.models.workout



/**
 * Data class representing the social statistics of a workout.
 *
 * @property likesCount The number of likes the workout has received.
 * @property saveCount The number of times the workout has been saved.
 */
data class WorkoutMetrics(
    val likesCount: Int = 0,
    val saveCount: Int = 0
)