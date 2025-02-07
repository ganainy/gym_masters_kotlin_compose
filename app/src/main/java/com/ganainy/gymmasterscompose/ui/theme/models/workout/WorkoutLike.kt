package com.ganainy.gymmasterscompose.ui.theme.models.workout


/**
 * Data class representing a like on a workout.
 *
 * @property id The unique identifier for the workout like.
 * @property userId The unique identifier of the user who liked the workout.
 * @property workoutId The unique identifier of the workout that was liked.
 * @property timestamp The timestamp when the workout was liked.
 */
data class WorkoutLike(
    val id: String = "", // Will be "$userId_$workoutId"
    val userId: String = "",
    val workoutId: String = "",
    val timestamp: Long = 0
)
 {
    companion object {
        fun createId(userId: String, workoutId: String) = "${userId}_${workoutId}"
        const val WORKOUT_LIKES_COLLECTION = "workout_likes" // Collection name for workout likes
    }
}