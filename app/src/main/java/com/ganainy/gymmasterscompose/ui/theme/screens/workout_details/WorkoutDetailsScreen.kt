package com.ganainy.gymmasterscompose.ui.theme.screens.workout_details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.ui.theme.components.LoadingIndicator
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.EmptyContent
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.ErrorContent

@Composable
fun WorkoutDetailsScreen(workout: Workout,
                         isLiked: Boolean,
                         isSaved: Boolean,
                         navigateToExerciseDetails: (Exercise) -> Unit) {

    val viewModel: WorkoutDetailsViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setWorkoutDetails(
            workout = workout,
            isLiked = isLiked,
            isSaved = isSaved
        )
        viewModel.observeWorkoutChanges()
    }


    if (uiState.isLoading) {
        LoadingIndicator()
    }
    if (uiState.error != null) {
        ErrorContent(
            message = uiState.error!!,
            onRetry = {}
        )
    } else {
        if (uiState.workoutWithStatus ==null) {
            EmptyContent(
            )
        } else {
            WorkoutDetailsContent(
               workoutWithStatus = uiState.workoutWithStatus,
                onWorkoutLike = viewModel::toggleWorkoutLike,
                onWorkoutSave =viewModel::toggleWorkoutSave ,
                navigateToExerciseDetails = navigateToExerciseDetails
            )
        }


    }

}