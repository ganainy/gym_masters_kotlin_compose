package com.ganainy.gymmasterscompose.ui.theme.models



/**
 * Data class representing a saved workout.
 *
 * @property id The unique identifier for the saved workout.
 * @property userId The unique identifier of the user who saved the workout.
 * @property workoutId The unique identifier of the workout being saved.
 * @property timestamp The timestamp when the workout was saved.
 */
data class WorkoutSave(
    val id: String = "",
    val userId: String = "",
    val workoutId: String = "",
    val timestamp: Long = 0
)