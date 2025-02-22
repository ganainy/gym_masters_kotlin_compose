package com.ganainy.gymmasterscompose.ui.theme.screens.workout_details

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.EmptyContent
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.ErrorContent
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.WorkoutWithStatus
import com.ganainy.gymmasterscompose.ui.theme.shared_components.CustomTopAppBar
import com.ganainy.gymmasterscompose.ui.theme.shared_components.LoadingIndicator
import com.ganainy.gymmasterscompose.utils.MockData.sampleWorkoutExerciseList

@Composable
fun WorkoutDetailsScreen(
    workout: Workout,
    isLiked: Boolean,
    isSaved: Boolean,
    navigateBack: () -> Unit,
    navigateToExerciseDetails: (Exercise) -> Unit
) {

    val viewModel: WorkoutDetailsViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setWorkoutDetails(
            workout = workout, isLiked = isLiked, isSaved = isSaved
        )
        viewModel.observeWorkoutChanges()
    }

    WorkoutDetailsContent(uiState, navigateBack, navigateToExerciseDetails)


}

@Composable
private fun WorkoutDetailsContent(
    uiState: WorkoutDetailsUiData,
    navigateBack: () -> Unit,
    navigateToExerciseDetails: (Exercise) -> Unit
) {
    Column {
        CustomTopAppBar(
            title =  "Workout Details",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = { navigateBack() },
        )

        if (uiState.isLoading) {
            LoadingIndicator()
        }
        if (uiState.error != null) {
            ErrorContent(
                message = uiState.error,
                onRetry = {},
            )
        } else {
            if (uiState.workoutWithStatus == null) {
                EmptyContent(
                )
            } else {
                val workoutWithStatus = uiState.workoutWithStatus

                WorkoutDetailedComposable(
                    workoutWithStatus = workoutWithStatus,
                    detailedOnlyParams = DetailedOnlyParams(
                        onExerciseClick = navigateToExerciseDetails,
                    ),
                )

            }


        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutDetailsContent_Preview(
    uiState: WorkoutDetailsUiData = WorkoutDetailsUiData(
        workoutWithStatus = WorkoutWithStatus(
            workout = Workout(
                id = "1",
                title = "Sample Workout",
                description = "This is a sample workout description",
                workoutDuration = "30m",
                workoutExerciseList = sampleWorkoutExerciseList
            ), isLiked = false, isSaved = false
        ), isLoading = false, error = null
    ), navigateBack: () -> Unit = {}, navigateToExerciseDetails: (Exercise) -> Unit = {}
) {
    WorkoutDetailsContent(
        uiState = uiState,
        navigateBack = navigateBack,
        navigateToExerciseDetails = navigateToExerciseDetails
    )
}