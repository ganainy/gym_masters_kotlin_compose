package com.ganainy.gymmasterscompose.ui.screens.workout_list

import com.ganainy.gymmasterscompose.ui.models.workout.Workout

sealed interface WorkoutListScreenAction {
    data class OnQueryChange(val query: String) : WorkoutListScreenAction
    data class OnSortPreferenceSelected(val sortType: SortType) : WorkoutListScreenAction
    data class OnWorkoutClick(val workout: Workout, val isLiked: Boolean, val isSaved: Boolean) :
        WorkoutListScreenAction
    data object OnRetry : WorkoutListScreenAction
    data class OnWorkoutLike(val workout: Workout) : WorkoutListScreenAction
    data class OnWorkoutSave(val workout: Workout) : WorkoutListScreenAction
}