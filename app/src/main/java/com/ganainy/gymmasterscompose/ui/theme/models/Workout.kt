package com.ganainy.gymmasterscompose.ui.theme.models

data class Workout(
    val workoutId: String="",
    val userId: String="",
    val title: String="",
    val description: String="",
    val difficulty: String="",
    val workoutDuration:  String="", //mins
    val dateCreated: Long=0, // Timestamp
    val imageUrl: String="", // Firebase Storage URL
    var imagePath: String="", // Local image path
    val tags: List<String> = emptyList(),
    var workoutExerciseList: List<WorkoutExercise> = emptyList(),
    val isPublic: Boolean = false
)

data class WorkoutExercise(
    val exercise: Exercise,
    var order: Int=0,
    val sets: Int=0,
    val reps: Int=0,
    val restBetweenSets: Int=0,
)

data class WorkoutSocialStats(
    val workoutId: String,
    val upvotes: Int=0,
    val downvotes: Int=0,
)