package com.ganainy.gymmasterscompose.ui.theme.screens.create_workout

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.AppTheme
import com.ganainy.gymmasterscompose.ui.theme.components.HashtagOutlinedTextField
import com.ganainy.gymmasterscompose.ui.theme.components.LoadingIndicator
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.ui.theme.screens.create_workout.UiState.DataState
import com.ganainy.gymmasterscompose.utils.MockData.sampleWorkout
import com.ganainy.gymmasterscompose.utils.Utils.showToast


@Composable
fun WorkoutSetupScreen(
    modifier: Modifier = Modifier,
    navigateToFeed: () -> Unit,
    navigateBack: () -> Unit,
    navigateToExercise: (Exercise) -> Unit,
    viewModel: CreateWorkoutViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState: UiState.WorkoutUiState by viewModel.uiState.collectAsState()


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
            is CreateWorkoutAction.UploadWorkout -> viewModel.workoutManager.uploadWorkout()
            is CreateWorkoutAction.EditWorkout -> viewModel.workoutManager.editWorkout(
                event.workout
            )
        }
    }


    WorkoutSetupScaffold(
        uiState = uiState,
        onUploadClick = { onAction(CreateWorkoutAction.UploadWorkout) }
    ) { paddingValues ->
        when (val workoutState = uiState.workoutState) {

            is DataState.Error -> {
                showToast(context, workoutState.message)
            }

            DataState.Loading -> LoadingIndicator()
            DataState.WorkoutSetup -> WorkoutSetupContent(
                paddingValues = paddingValues,
                uiState = uiState,
                onAction = onAction,
            )

            DataState.ExerciseListSetup -> WorkoutExerciseListScreenContent(paddingValues,uiState, onAction)
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutSetupScaffold(
    uiState: UiState.WorkoutUiState,
    onUploadClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            WorkoutSetupTopBar(
                isUploadEnabled = uiState.isUploadEnabled,
                onUploadClick = onUploadClick
            )
        },
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSetupTopBar(isUploadEnabled: Boolean, onUploadClick: () -> Unit) {
    TopAppBar(
        title = { Text("Create New Workout") },
        actions = {
            TextButton(
                onClick = {
                    onUploadClick()
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
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
            WorkoutAddExercisesSection(
                onAddExercise =
                { onAction(CreateWorkoutAction.ToggleExerciseWorkoutListShow) },
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
            )

        }

        item {
            WorkoutVisibilityToggle(
                uiState.workout,
                { onAction(CreateWorkoutAction.EditWorkout(it)) })
        }
    }
}

@Composable
fun WorkoutAddExercisesSection(onAddExercise: () -> Unit) {
    Column {
        Text(
            "Exercises",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Button(
            onClick = onAddExercise,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Exercise")
        }

    }
}

@Composable
fun WorkoutExercisesSection(
    exerciseList: List<WorkoutExercise>,
    onDeleteExercise: (WorkoutExercise) -> Unit
) {
    if (exerciseList.isNullOrEmpty()) {
        Text("No exercises added yet")
    } else {
        exerciseList.forEach { exercise ->
            ExerciseCard(exercise, onDeleteExercise = onDeleteExercise)
        }
    }
}

@Composable
fun WorkoutVisibilityToggle(workout: Workout, onEditWorkout: (Workout) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = workout.isPublic,
            onCheckedChange = { onEditWorkout(workout.copy(isPublic = it)) },
        )
        Text("Make workout public")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutTagsSection(
    currentTag: String,
    workoutTags: List<String>,
    onCurrentTagTextChange: (String) -> Unit,
    onTagAdd: () -> Unit,
    onTagRemove: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentTag,
                onValueChange = onCurrentTagTextChange,
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
            workoutTags.forEach { tag ->
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

        WorkoutCoverImageContent(workout, onEditWorkout)
    }
}

@Composable
private fun WorkoutCoverImageContent(
    workout: Workout,
    onEditWorkout: (Workout) -> Unit,
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(128.dp)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            if (workout.imagePath.isNotEmpty()) {
                Image(
                    painter = rememberImagePainter(data = workout.imagePath),
                    contentDescription = "Workout cover image",
                    modifier = Modifier.matchParentSize()
                )
                IconButton(
                    onClick = {
                        onEditWorkout(workout.copy(imagePath = ""))
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove image"
                    )
                }
            } else {
                val drawable = context.getDrawable(R.drawable.dumbbell)
                val imageBitmap = drawable?.toBitmap()?.asImageBitmap()
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Workout cover image placeholder",
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri: Uri? ->
                if (uri != null) {
                    onEditWorkout(workout.copy(imagePath = uri.toString()))
                }
            }
        )

        IconButton(onClick = {
            launcher.launch("image/*")
        }) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Select image"
            )
        }
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


@Composable
private fun ExerciseCard(
    workoutExercise: WorkoutExercise,
    onDeleteExercise: (WorkoutExercise) -> Unit,
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
                workoutExercise.exercise?.name?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                IconButton(onClick = { onDeleteExercise(workoutExercise) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete exercise")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sets: ${workoutExercise.sets} | Reps: ${workoutExercise.reps}")
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewWorkoutSetupContent() {
    AppTheme {
        WorkoutSetupContent(
            modifier = Modifier,
            uiState = UiState.WorkoutUiState(),
            onAction = {},
            paddingValues = PaddingValues(0.dp),
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun WorkoutSetupContentWithExercisesPreview() {
    AppTheme {
        WorkoutSetupContent(
            modifier = Modifier,
            uiState = UiState.WorkoutUiState(workout = sampleWorkout),
            onAction = {},
            paddingValues = PaddingValues(0.dp),
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun WorkoutSetupContentWithExercisesPreview2() {
    AppTheme {
        WorkoutSetupContent(
            modifier = Modifier,
            uiState = UiState.WorkoutUiState(workoutState = DataState.ExerciseListSetup),
            onAction = {},
            paddingValues = PaddingValues(0.dp),
        )
    }
}