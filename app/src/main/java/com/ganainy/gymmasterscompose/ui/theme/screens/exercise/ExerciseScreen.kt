package com.ganainy.gymmasterscompose.ui.theme.screens.exercise

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.shared_components.EmptyComponent
import com.ganainy.gymmasterscompose.ui.theme.shared_components.ErrorComponent
import com.ganainy.gymmasterscompose.ui.theme.shared_components.LoadingIndicator
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(exercise: Exercise, navigateBack: () -> Unit) {

    val viewModel = hiltViewModel<ExerciseViewModel>()

    LaunchedEffect(Unit) {
        viewModel.setExercise(exercise)
    }

    val uiState by viewModel.uiState.collectAsState()


    // Main Screen Content
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ExercisesContent(uiState, viewModel::toggleExerciseSave, navigateBack)
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ExercisesContent(
    uiState: ExerciseViewModel.ExerciseUiState,
    onSaveExercise: () -> Unit,
    navigateBack: () -> Unit,
) {

    val exerciseWithSaveState = uiState.exercise


    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Exercise") },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = onSaveExercise) {
                    Icon(
                        painter = if (exerciseWithSaveState?.isSavedLocally == true) painterResource(id = R.drawable.save_filled) else painterResource(id = R.drawable.save_outlined),
                        contentDescription = "Save"
                    )
                }
            }
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


