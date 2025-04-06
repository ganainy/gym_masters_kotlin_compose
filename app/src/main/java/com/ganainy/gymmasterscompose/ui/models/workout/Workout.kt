package com.ganainy.gymmasterscompose.ui.models.workout

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

/**
 * Data class representing a workout.
 *
 * @property id The unique identifier for the workout.
 * @property creatorId The unique identifier of the user who created the workout.
 * @property title The title of the workout.
 * @property description A description of the workout.
 * @property difficulty The difficulty level of the workout.
 * @property workoutDuration The duration of the workout in minutes.
 * @property dateCreated The timestamp when the workout was created.
 * @property imageUrl The URL of the workout image stored in Firebase Storage.
 * @property imagePath The local path of the workout image.
 * @property tags A list of tags associated with the workout.
 * @property isPublic A boolean indicating whether the workout is public.
 * @property workoutExerciseList A list of exercises included in the workout.
 * @property workoutMetrics The social statistics of the workout (upvotes, downvotes, save count).
 */

@Parcelize
data class Workout(
    val id: String = "",
    val creatorId: String = "",
    val title: String = "",
    val description: String = "",
    val difficulty: String = "",
    val workoutDuration: String = "", // mins
    val dateCreated: Timestamp = Timestamp.now(), // Timestamp
    val imageUrl: String = "", // Firebase Storage URL
    var imagePath: String = "", // Local image path
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = false,
    var workoutExerciseList: List<WorkoutExercise> = emptyList(),
    val workoutMetrics: WorkoutMetrics = WorkoutMetrics(),
) : Parcelable


@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey
    val id: String,
    val creatorId: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val workoutDuration: String,
    val dateCreated: Timestamp,
    val imageUrl: String,
    val imagePath: String,
    val tags: List<String>,
    val isPublic: Boolean,
    val workoutExerciseList: List<WorkoutExercise>,
    @Embedded // Embed WorkoutMetrics directly in the table
    val workoutMetrics: WorkoutMetrics
) {
    companion object {
        const val WORKOUTS_COLLECTION = "workouts"
        const val WORKOUT_LIKES_COUNT = "likesCount"
        const val WORKOUT_SAVE_COUNT = "saveCount"
        const val WORKOUTS_METRICS = "workoutMetrics"
        const val WORKOUTS_TIMESTAMP = "dateCreated"
    }
}




// Extension functions to convert between Workout and WorkoutEntity
fun Workout.toWorkoutEntity(): WorkoutEntity {
    return WorkoutEntity(
        id = id,
        creatorId = creatorId,
        title = title,
        description = description,
        difficulty = difficulty,
        workoutDuration = workoutDuration,
        dateCreated = dateCreated,
        imageUrl = imageUrl,
        imagePath = imagePath,
        tags = tags,
        isPublic = isPublic,
        workoutExerciseList = workoutExerciseList,
        workoutMetrics = workoutMetrics
    )
}

fun WorkoutEntity.toWorkout(): Workout {
    return Workout(
        id = id,
        creatorId = creatorId,
        title = title,
        description = description,
        difficulty = difficulty,
        workoutDuration = workoutDuration,
        dateCreated = dateCreated,
        imageUrl = imageUrl,
        imagePath = imagePath,
        tags = tags,
        isPublic = isPublic,
        workoutExerciseList = workoutExerciseList,
        workoutMetrics = workoutMetrics
    )
}



