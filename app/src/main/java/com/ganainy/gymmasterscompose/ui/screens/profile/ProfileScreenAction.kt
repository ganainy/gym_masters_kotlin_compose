package com.ganainy.gymmasterscompose.ui.screens.profile

import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.screens.workout_list.WorkoutWithStatus

sealed interface ProfileScreenAction {
    data object NavigateToLogin : ProfileScreenAction
    data object NavigateBack : ProfileScreenAction
    data class NavigateToEditProfile(val imagePath: String) : ProfileScreenAction
    data object NavigateToWorkoutSetup : ProfileScreenAction
    data class NavigateToWorkoutDetails(val workoutWithStatus: WorkoutWithStatus) :
        ProfileScreenAction
    data object NavigateToExercisesList : ProfileScreenAction
    data class NavigateToExerciseDetails(val exercise: Exercise) : ProfileScreenAction
    data class ToggleFollow(val userToFollowOrUnfollowId: String) : ProfileScreenAction
    data object Logout : ProfileScreenAction
    data object NavigateToCreatePost : ProfileScreenAction
}

