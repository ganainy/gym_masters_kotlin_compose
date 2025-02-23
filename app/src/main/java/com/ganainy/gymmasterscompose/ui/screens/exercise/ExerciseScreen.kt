package com.ganainy.gymmasterscompose.ui.screens.exercise

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.shared_components.CustomTopAppBar
import com.ganainy.gymmasterscompose.ui.shared_components.EmptyComponent
import com.ganainy.gymmasterscompose.ui.shared_components.ErrorComponent
import com.ganainy.gymmasterscompose.ui.shared_components.LoadingIndicator
import com.ganainy.gymmasterscompose.utils.MockData.sampleExercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(exercise: Exercise, navigateBack: () -> Unit) {

    val viewModel = hiltViewModel<ExerciseViewModel>()

    LaunchedEffect(Unit) {
        viewModel.setExercise(exercise)
    }

    val uiState by viewModel.uiState.collectAsState()


    // Main Screen Content

        ExerciseListContent(uiState, viewModel::toggleExerciseSave, navigateBack)
}


@Composable
private fun ExerciseListContent(
    uiState: ExerciseViewModel.ExerciseUiState,
    onSaveExercise: () -> Unit,
    navigateBack: () -> Unit,
) {

    val exerciseWithSaveState = uiState.exercise


    Column(modifier = Modifier.fillMaxSize()) {

        CustomTopAppBar(
            title = "Exercise",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = navigateBack,
            actionDrawables = listOf(
                if (exerciseWithSaveState?.isSavedLocally == true) R.drawable.save_filled else R.drawable.save_outlined,
            ),
            onActionClicks = listOf { onSaveExercise() },

        )

        if (uiState.isLoading) {
            LoadingIndicator()
        } else if (uiState.error != null) {
            ErrorComponent(text = uiState.error)
        } else if (exerciseWithSaveState != null) {
            ExerciseContent(
                exerciseWithSaveState,
            )
        } else {
            EmptyComponent("No exercises found")
        }
    }

}


@Preview(showBackground = true)
@Composable
private fun PreviewExerciseListContent() {
    ExerciseListContent(
        uiState = ExerciseViewModel.ExerciseUiState(
            exercise = sampleExercise,
            isLoading = false,
            error = null
        ),
        onSaveExercise = { },
        navigateBack = { }
    )
}