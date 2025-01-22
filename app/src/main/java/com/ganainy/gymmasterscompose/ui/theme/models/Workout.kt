package com.ganainy.gymmasterscompose.ui.theme.models

data class Workout(
    val workoutId: String,
    val userId: String,
    val title: String,
    val description: String,
    val difficultyLevel: String,
    val duration: Int,
    val dateCreated: Long, // Timestamp
    val imageUrl: String,
    val tags: List<String>,
    val exercises: List<WorkoutExercise>,
    val isPublic: Boolean
)

data class WorkoutExercise(
    val exercise: ExerciseSummary,
    val order: Int,
    val sets: Int,
    val reps: Int,
    val duration: Int,
    val restBetweenSets: Int
)

data class ExerciseSummary(
    val exerciseId: String,
    val name: String,
    val description: String,
    val difficulty: String,
    val muscleGroup: String,
    val imageUrl: String,
    val imageUrl2: String,
    val additionalNotes: String
)