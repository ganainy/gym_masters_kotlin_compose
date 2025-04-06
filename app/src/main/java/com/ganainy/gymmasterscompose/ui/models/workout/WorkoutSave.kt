package com.ganainy.gymmasterscompose.ui.models.workout

import com.google.firebase.Timestamp


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
    val timestamp: Timestamp = Timestamp.now(),
){
    companion object {
        fun createId(userId: String, workoutId: String) = "${userId}_${workoutId}"
        const val WORKOUT_ID_FIELD = "workoutId"
        const val WORKOUT_SAVES_COLLECTION = "workout_saves" // Collection name for workout saves
    }
}