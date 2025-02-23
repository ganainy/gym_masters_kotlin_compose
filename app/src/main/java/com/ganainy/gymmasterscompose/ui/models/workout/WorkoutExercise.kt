package com.ganainy.gymmasterscompose.ui.models.workout

import android.os.Parcelable
import com.ganainy.gymmasterscompose.ui.models.Exercise
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Data class representing an exercise in a workout.
 *
 * @property exercise The exercise details.
 * @property order The order of the exercise in the workout.
 * @property sets The number of sets for the exercise.
 * @property reps The number of repetitions per set for the exercise.
 * @property restBetweenSets The rest time between sets in seconds.
 */

@Serializable
@Parcelize
data class WorkoutExercise(
    val exercise: Exercise? = null,
    var order: Int = 0,
    val sets: Int = 0,
    val reps: Int = 0,
    val restBetweenSets: Int = 0,
) : Parcelable