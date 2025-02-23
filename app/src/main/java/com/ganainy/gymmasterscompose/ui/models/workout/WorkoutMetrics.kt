package com.ganainy.gymmasterscompose.ui.models.workout

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * Data class representing the social statistics of a workout.
 *
 * @property likesCount The number of likes the workout has received.
 * @property saveCount The number of times the workout has been saved.
 */
@Parcelize
data class WorkoutMetrics(
    val likesCount: Int = 0,
    val saveCount: Int = 0
) : Parcelable {
    companion object {
        const val WORKOUT_LIKES_COUNT = "likesCount"
        const val WORKOUT_SAVES_COUNT = "saveCount"
    }
}