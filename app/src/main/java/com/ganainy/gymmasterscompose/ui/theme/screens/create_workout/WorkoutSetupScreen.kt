package com.ganainy.gymmasterscompose.ui.theme.screens.create_workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.ui.theme.components.LoadingIndicator
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.WorkoutExercise
import com.ganainy.gymmasterscompose.utils.Utils.showToast

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkoutSetupScreen(
    modifier: Modifier = Modifier,
    navigateToCreateWorkoutExerciseList: () -> Unit,
    navigateToFeed: () -> Unit,
    viewModel: CreateWorkoutViewModel
) {

    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()

    val workoutExerciseList = uiState.workoutExerciseList

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Workout") },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.uploadWorkout()
                        },
                        enabled = uiState.workout.title.isNotBlank() && workoutExerciseList.isNotEmpty()
                    ) {
                        Text("Upload")
                    }
                }
            )
        }
    ) { paddingValues ->


        when (val workoutState = uiState.workoutState){
            is WorkoutDataState.Error -> {
                    showToast(context,  (uiState.workoutState as WorkoutDataState.Error).message)
                    viewModel.clearError()
            }
            is WorkoutDataState.Initial -> Unit
            is WorkoutDataState.Loading -> {
                    LoadingIndicator()
            }
            is WorkoutDataState.Success -> {
                CreateWorkoutContent(
                    modifier,
                    paddingValues,
                    uiState,
                    navigateToCreateWorkoutExerciseList,
                    workoutExerciseList,
                    onEditWorkout = viewModel::editWorkout,
                    availableDifficulties = uiState.availableDifficulties,
                    onDifficultyLevelChange = viewModel::changeWorkoutDifficulty,
                    currentDifficulty = uiState.currentDifficulty,
                    onCurrentTagTextChange = viewModel::changeCurrentTagText,
                    onTagAdd =  viewModel::addTag,
                    onTagRemove =  viewModel::removeTag,
                    onDeleteWorkoutExercise =  viewModel::deleteWorkoutExercise
                )
            }
        }

}
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun CreateWorkoutContent(
    modifier: Modifier,
    paddingValues: PaddingValues,
    uiState:  WorkoutUiState,
    navigateToCreateWorkoutExerciseList: () -> Unit,
    workoutExerciseList: List<WorkoutExercise>,
    onEditWorkout: (Workout) -> Unit,
    availableDifficulties: List<String>,
    onDifficultyLevelChange: (String) -> Unit,
    currentDifficulty: String,
    onCurrentTagTextChange: (String) -> Unit,
    onTagAdd: () -> Unit,
    onTagRemove: (String) -> Unit,
    onDeleteWorkoutExercise: (WorkoutExercise) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = uiState.workout.title,
                onValueChange = { onEditWorkout(uiState.workout.copy(title = it)); },
                label = { Text("Workout Title") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = uiState.workout.description,
                onValueChange = { onEditWorkout(uiState.workout.copy(description = it)); },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }

        item {
            DifficultyDropdownMenu(
                availableDifficulties, onDifficultyLevelChange,
                currentDifficulty = currentDifficulty
            )
        }

        item {
            OutlinedTextField(
                value = uiState.workout.workoutDuration,
                onValueChange = { onEditWorkout(uiState.workout.copy(workoutDuration = it)); },
                label = { Text("Duration (minutes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = uiState.workout.imagePath,
                onValueChange = { onEditWorkout(uiState.workout.copy(imagePath = it)); },
                label = { Text("Image path") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Tags Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.currentTag,
                        onValueChange =onCurrentTagTextChange,
                        label = { Text("Add Tag") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onTagAdd
                    ) {
                        Text("Add")
                    }
                }

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.workout.tags.forEach { tag ->
                        AssistChip(
                            onClick = { onTagRemove(tag) },
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove tag"
                                )
                            }
                        )
                    }
                }
            }
        }

        // Exercises Section
        item {
            Column {
                Text(
                    "Exercises",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(
                    onClick = navigateToCreateWorkoutExerciseList,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Exercise")
                }
            }
        }

        items(workoutExerciseList) { workoutExercise ->
            ExerciseCard(
                workoutExercise = workoutExercise,
                onDelete = { onDeleteWorkoutExercise(workoutExercise) },
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.workout.isPublic,
                    onCheckedChange = { onEditWorkout(uiState.workout.copy(isPublic = it)); },
                )
                Text("Make workout public")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultyDropdownMenu(
    difficulties: List<String>,
    onDifficultyLevelChange: (String) -> Unit,
    currentDifficulty: String
) {
    var expanded by remember { mutableStateOf(false) } // State to manage dropdown visibility

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }, // Toggle expanded state
    ) {
        OutlinedTextField(
            value = currentDifficulty,
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
                        onDifficultyLevelChange(difficulty) // Update the ViewModel
                        expanded = false // Close the dropdown
                    }
                )
            }
        }
    }
}


@Composable
private fun ExerciseCard(
    workoutExercise: WorkoutExercise,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workoutExercise.exercise.name,
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete exercise")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sets: ${workoutExercise.sets} | Reps: ${workoutExercise.reps}")
        }
    }
}

@Preview
@Composable
private fun PreviewExerciseCard() {
    val exercise = WorkoutExercise(
        exercise = Exercise(
            bodyPart = "Chest",
            equipment = "Barbell",
            gifUrl = "url_to_bench_press_gif",
            id = "1",
            name = "Bench Press",
            target = "Pectoralis major",
            secondaryMuscles = listOf("Anterior deltoid", "Serratus anterior"),
            instructions = listOf("Press the barbell upwards")
        ),
        order = 1,
        sets = 3,
        reps = 8,
        restBetweenSets = 60
    )
    ExerciseCard(exercise, onDelete = {})
}


