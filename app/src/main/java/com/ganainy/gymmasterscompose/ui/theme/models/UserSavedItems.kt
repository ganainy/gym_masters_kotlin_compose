package com.ganainy.gymmasterscompose.ui.theme.models

data class UserSavedItems(
    val exercises: Map<String, Long>, // ExerciseId to timestamp
    val workouts: Map<String, Long>  // WorkoutId to timestamp
)