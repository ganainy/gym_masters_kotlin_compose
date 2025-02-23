package com.ganainy.gymmasterscompose.ui.screens.create_workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.screens.create_workout.UiState.DataState
import com.ganainy.gymmasterscompose.ui.screens.create_workout.composables.WorkoutCoverImageContent
import com.ganainy.gymmasterscompose.ui.screens.create_workout.composables.WorkoutExerciseListScreenContent
import com.ganainy.gymmasterscompose.ui.screens.create_workout.composables.WorkoutExercisesSection
import com.ganainy.gymmasterscompose.ui.shared_components.HashtagOutlinedTextField
import com.ganainy.gymmasterscompose.ui.shared_components.LoadingIndicator
import com.ganainy.gymmasterscompose.utils.MockData
import com.ganainy.gymmasterscompose.utils.MockData.sampleExercise
import com.ganainy.gymmasterscompose.utils.MockData.sampleWorkoutExercise
import com.ganainy.gymmasterscompose.utils.Utils.showToast


@Composable
fun WorkoutSetupScreen(
    modifier: Modifier = Modifier,
    navigateToFeed: () -> Unit,
    navigateBack: () -> Unit,
    navigateToExercise: (Exercise) -> Unit,
    viewModel: CreateWorkoutViewModel = hiltViewModel(),
) {
    val uiState: UiState.WorkoutUiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Event handler
    val onAction: (CreateWorkoutAction) -> Unit = { event ->
        when (event) {
            is CreateWorkoutAction.NavigateBack -> navigateBack()
            is CreateWorkoutAction.ShowFilterSheet -> {
                viewModel.filterManager.showFilterSheet()
            }

            is CreateWorkoutAction.SearchQueryChanged -> {
                viewModel.filterManager.onQueryChange(event.query)
            }

            is CreateWorkoutAction.ExerciseSelected -> {
                viewModel.exerciseManager.setSelectedExercise(event.exercise)
            }

            is CreateWorkoutAction.ExerciseModified -> {
                viewModel.exerciseManager.editWorkoutExercise(event.exercise)
            }

            is CreateWorkoutAction.ExerciseDeleted -> {
                viewModel.exerciseManager.deleteWorkoutExercise(event.exercise)
            }

            is CreateWorkoutAction.DismissAddExerciseDialog -> {
                viewModel.exerciseManager.dismissAddExerciseDialog()
            }

            is CreateWorkoutAction.AddWorkoutExercise -> {
                viewModel.exerciseManager.addWorkoutExercise(event.exercise)
            }

            is CreateWorkoutAction.BodyPartFilterChange -> {
                viewModel.filterManager.onBodyPartFilterChange(event.bodyPart)
            }

            CreateWorkoutAction.ApplyFilters -> viewModel.filterManager.applyFilters()
            is CreateWorkoutAction.EquipmentFilterChange -> {
                viewModel.filterManager.onEquipmentFilterChange(event.equipment)
            }

            is CreateWorkoutAction.TargetMuscleFilterChange -> {
                viewModel.filterManager.onTargetMuscleFilterChange(event.targetMuscle)
            }

            CreateWorkoutAction.ClearFilters -> viewModel.filterManager.clearFilters()
            is CreateWorkoutAction.EditWorkoutExercise -> {
                viewModel.exerciseManager.editWorkoutExercise(event.exercise)
            }

            is CreateWorkoutAction.NavigateToExercise -> navigateToExercise(event.exercise)
            CreateWorkoutAction.HideFilterSheet -> viewModel.filterManager.hideFilterSheet()

            is CreateWorkoutAction.ToggleExerciseWorkoutListShow -> viewModel.toggleExerciseWorkoutListShow()
            is CreateWorkoutAction.NavigateToFeed -> navigateToFeed()
            is CreateWorkoutAction.UploadWorkout -> viewModel.workoutManager.uploadWorkout(onSuccess = {
                showToast(context, context.getString(R.string.workout_uploaded_successfully))
                navigateToFeed()
            })
            is CreateWorkoutAction.EditWorkout -> viewModel.workoutManager.editWorkout(
                event.workout
            )
        }
    }



    WorkoutSetupScreenContent(uiState, onAction)

}

@Composable
private fun WorkoutSetupScreenContent(
    uiState: UiState.WorkoutUiState,
    onAction: (CreateWorkoutAction) -> Unit
) {
    val context = LocalContext.current

    when (val workoutState = uiState.workoutState) {

        is DataState.Error -> {
            showToast(context, workoutState.message)
        }

        is DataState.Loading -> LoadingIndicator()
        is DataState.WorkoutSetup -> Scaffold(
            topBar = {
                WorkoutSetupTopBar(
                    isUploadEnabled = uiState.isUploadEnabled,
                    onAction = onAction
                )
            },
            content = { paddingValues ->
                WorkoutSetupContent(
                    paddingValues = paddingValues,
                    uiState = uiState,
                    onAction = onAction,
                )
            }
        )

        is DataState.ExerciseListSetup -> WorkoutExerciseListScreenContent(uiState, onAction)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSetupTopBar(isUploadEnabled: Boolean, onAction: (CreateWorkoutAction) -> Unit) {
    TopAppBar(
        title = { Text("Create New Workout") },
        actions = {
            TextButton(
                onClick = {
                    onAction(CreateWorkoutAction.UploadWorkout)
                },
                enabled = isUploadEnabled
            ) {
                Text("Upload")
            }
        }
    )
}

@Composable
private fun WorkoutSetupContent(
    modifier: Modifier = Modifier,
    uiState: UiState.WorkoutUiState,
    onAction: (CreateWorkoutAction) -> Unit,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item {
            WorkoutCoverImageContent(workout = uiState.workout,
                onEditWorkout = { onAction(CreateWorkoutAction.EditWorkout(it)) })
        }

        item {
            WorkoutBasicInfo(
                workout = uiState.workout,
                onEditWorkout = { onAction(CreateWorkoutAction.EditWorkout(it)) })
        }
        item {
            DifficultyDropdownMenu(
                difficulties = uiState.availableDifficulties,
                onEditWorkout = { onAction(CreateWorkoutAction.EditWorkout(it)) },
                workout = uiState.workout
            )
        }


        item {
            WorkoutExercisesSection(
                exerciseList = uiState.workout.workoutExerciseList,
                onDeleteExercise = {
                    onAction(
                        CreateWorkoutAction.ExerciseDeleted(
                            it
                        )
                    )
                },
                toggleExerciseWorkoutListShow = { onAction(CreateWorkoutAction.ToggleExerciseWorkoutListShow) }
            )

        }
    }
}




@Composable
private fun WorkoutBasicInfo(workout: Workout, onEditWorkout: (Workout) -> Unit) {
    Column {
        HashtagOutlinedTextField(
            value = workout.title,
            onValueChange = { onEditWorkout(workout.copy(title = it)) },
            label = stringResource(R.string.workout_title),
            modifier = Modifier.fillMaxWidth()
        )

        HashtagOutlinedTextField(
            value = workout.description,
            onValueChange = { onEditWorkout(workout.copy(description = it)) },
            label = stringResource(R.string.description),
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = workout.workoutDuration,
            onValueChange = {
                if (it.toIntOrNull() != null) {
                    onEditWorkout(workout.copy(workoutDuration = it))
                }
            },
            label = { Text(stringResource(R.string.duration_minutes)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )


    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultyDropdownMenu(
    difficulties: List<String>,
    onEditWorkout: (Workout) -> Unit,
    workout: Workout
) {
    var expanded by remember { mutableStateOf(false) } // State to manage dropdown visibility

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }, // Toggle expanded state
    ) {
        OutlinedTextField(
            value = workout.difficulty,
            onValueChange = {}, // No need to handle this since it's readOnly
            readOnly = true, // Make the text field read-only
            label = { Text("Difficulty Level") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor() // Anchor for the dropdown
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }, // Close the dropdown on dismiss
        ) {
            difficulties.forEach { difficulty ->
                DropdownMenuItem(
                    text = { Text(difficulty) },
                    onClick = {
                        onEditWorkout(workout.copy(difficulty = difficulty)) // Update the ViewModel
                        expanded = false // Close the dropdown
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewWorkoutSetupContentNoExercises() {
    AppTheme {
        WorkoutSetupScreenContent(
            uiState = UiState.WorkoutUiState(workoutState = DataState.WorkoutSetup),
            onAction = {},
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun WorkoutSetupContentWithExercisesPreview2() {
    AppTheme {
        WorkoutSetupScreenContent(
            uiState = UiState.WorkoutUiState(
                workoutState = DataState.ExerciseListSetup,
                workout = MockData.sampleWorkout,
                filteredExerciseList = MockData.sampleExerciseListLarge
            ),
            onAction = {},
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun WorkoutExercisesSectionPreview() {
    AppTheme {
        WorkoutExercisesSection(
            exerciseList = listOf(
             sampleWorkoutExercise,
                sampleWorkoutExercise.copy(exercise = sampleExercise.copy(name = "Exercise 2"))
            ),
            onDeleteExercise = { /* Handle delete */ },
            toggleExerciseWorkoutListShow = { /* Handle toggle */ }
        )
    }
}