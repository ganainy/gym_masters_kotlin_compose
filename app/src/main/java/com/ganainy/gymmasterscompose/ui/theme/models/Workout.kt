package com.ganainy.gymmasterscompose.ui.theme.models

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class Workout(
    val workoutId: String = "",
    val userId: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val dateCreated: Long = 0L,
    val duration: Duration = 0.minutes,
    val difficultyLevel: DifficultyLevel = DifficultyLevel.BEGINNER,
    val exercises: List<ExerciseInWorkout> = emptyList(),
    val tags: Set<String>? = null // Optional: for categorizing workouts
)

data class ExerciseInWorkout(
    val exercise: Exercise = Exercise(),
    val sets: Int = 0,
    val reps: Int = 0
)


enum class DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT
}