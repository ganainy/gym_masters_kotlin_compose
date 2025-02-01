package com.ganainy.gymmasterscompose.ui.theme.models

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
    data class Workout(
    val id: String = "",
    val creatorId: String = "",
    val title: String = "",
    val description: String = "",
    val difficulty: String = "",
    val workoutDuration: String = "", // mins
    val dateCreated: Long = 0, // Timestamp
    val imageUrl: String = "", // Firebase Storage URL
    var imagePath: String = "", // Local image path
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = false,
    var workoutExerciseList: List<WorkoutExercise> = emptyList(),
    val workoutMetrics: WorkoutMetrics = WorkoutMetrics(),
    ){
        companion object {
            const val WORKOUTS = "workouts"
        }
    }

    /**
     * Data class representing an exercise in a workout.
     *
     * @property exercise The exercise details.
     * @property order The order of the exercise in the workout.
     * @property sets The number of sets for the exercise.
     * @property reps The number of repetitions per set for the exercise.
     * @property restBetweenSets The rest time between sets in seconds.
     */
    data class WorkoutExercise(
        val exercise: Exercise,
        var order: Int = 0,
        val sets: Int = 0,
        val reps: Int = 0,
        val restBetweenSets: Int = 0,
    )

    /**
     * Data class representing the social statistics of a workout.
     *
     * @property upvotes The number of upvotes the workout has received.
     * @property downvotes The number of downvotes the workout has received.
     * @property saveCount The number of times the workout has been saved.
     */
    data class WorkoutMetrics(
        val upvotes: Int = 0,
        val downvotes: Int = 0,
        val saveCount: Int = 0
    )