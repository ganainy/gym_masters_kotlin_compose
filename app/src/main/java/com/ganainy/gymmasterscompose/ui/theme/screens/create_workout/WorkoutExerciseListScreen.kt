package com.ganainy.gymmasterscompose.ui.theme.screens.create_workout

import CustomSearchBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ganainy.gymmasterscompose.ui.theme.components.ChipOptionList
import com.ganainy.gymmasterscompose.ui.theme.components.EmptyComponent
import com.ganainy.gymmasterscompose.ui.theme.components.ErrorComponent
import com.ganainy.gymmasterscompose.ui.theme.components.ExerciseListItem
import com.ganainy.gymmasterscompose.ui.theme.components.ExerciseListItemType
import com.ganainy.gymmasterscompose.ui.theme.components.LoadingIndicator
import com.ganainy.gymmasterscompose.ui.theme.models.BodyPart
import com.ganainy.gymmasterscompose.ui.theme.models.Equipment
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.utils.Utils.showToast
import kotlinx.coroutines.launch


// UI States
sealed interface ExerciseListScreenEvent {
    object NavigateBack : ExerciseListScreenEvent
    object ShowFilterSheet : ExerciseListScreenEvent
    object DismissFilterSheet : ExerciseListScreenEvent
    object SaveWorkout : ExerciseListScreenEvent
    data class SearchQueryChanged(val query: String) : ExerciseListScreenEvent
    data class ExerciseSelected(val exercise: Exercise) : ExerciseListScreenEvent
    data class ExerciseModified(val exercise: WorkoutExercise) : ExerciseListScreenEvent
    data class ExerciseDeleted(val exercise: WorkoutExercise) : ExerciseListScreenEvent
    object DismissAddExerciseDialog : ExerciseListScreenEvent
    object ApplyFilters : ExerciseListScreenEvent
    object clearFilters : ExerciseListScreenEvent
    data class AddWorkoutExercise(val exercise: WorkoutExercise?) : ExerciseListScreenEvent
    data class EditWorkoutExercise(val exercise: WorkoutExercise?) : ExerciseListScreenEvent
    data class BodyPartFilterChange(val bodyPart: BodyPart) : ExerciseListScreenEvent
    data class EquipmentFilterChange(val equipment: Equipment) : ExerciseListScreenEvent
    data class TargetMuscleFilterChange(val targetMuscle: TargetMuscle) : ExerciseListScreenEvent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutExerciseListScreen(
    navigateBack: () -> Unit,
    viewModel: CreateWorkoutViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    // Event handler
    val handleEvent: (ExerciseListScreenEvent) -> Unit = { event ->
        when (event) {
            is ExerciseListScreenEvent.NavigateBack -> navigateBack()
            is ExerciseListScreenEvent.ShowFilterSheet -> {
                coroutineScope.launch { sheetState.show() }
            }
            is ExerciseListScreenEvent.DismissFilterSheet -> {
                coroutineScope.launch { sheetState.hide() }
            }
            is ExerciseListScreenEvent.SaveWorkout -> {
                navigateBack()
            }
            is ExerciseListScreenEvent.SearchQueryChanged -> {
                viewModel.filterManager.onQueryChange(event.query)
            }
            is ExerciseListScreenEvent.ExerciseSelected -> {
                viewModel.exerciseManager.setSelectedExercise(event.exercise)
            }
            is ExerciseListScreenEvent.ExerciseModified -> {
                viewModel.exerciseManager.editWorkoutExercise(event.exercise)
            }
            is ExerciseListScreenEvent.ExerciseDeleted -> {
                viewModel.exerciseManager.deleteWorkoutExercise(event.exercise)
            }
            is ExerciseListScreenEvent.DismissAddExerciseDialog -> {
                viewModel.exerciseManager.dismissAddExerciseDialog()
            }
            is ExerciseListScreenEvent.AddWorkoutExercise -> {
                viewModel.exerciseManager.addWorkoutExercise(event.exercise)
        }

            is ExerciseListScreenEvent.BodyPartFilterChange -> {
                viewModel.filterManager.onBodyPartFilterChange(event.bodyPart)
            }

            ExerciseListScreenEvent.ApplyFilters -> viewModel.filterManager.applyFilters()
            is ExerciseListScreenEvent.EquipmentFilterChange -> {
                viewModel.filterManager.onEquipmentFilterChange(event.equipment)
            }
            is ExerciseListScreenEvent.TargetMuscleFilterChange -> {
                viewModel.filterManager.onTargetMuscleFilterChange(event.targetMuscle)
            }
            ExerciseListScreenEvent.clearFilters -> viewModel.filterManager.clearFilters()
            is ExerciseListScreenEvent.EditWorkoutExercise -> {
                viewModel.exerciseManager.editWorkoutExercise(event.exercise)
            }
        }
    }

    // Error handling
    LaunchedEffect(uiState.exerciseListState) {
        if (uiState.exerciseListState is UiState.DataState.Error) {
            showToast(context, (uiState.exerciseListState as UiState.DataState.Error).message)
            viewModel.clearError()
        }
    }

    WorkoutExerciseListScreenContent(
        uiState = uiState,
        sheetState = sheetState,
        onEvent = handleEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutExerciseListScreenContent(
    uiState: UiState.WorkoutUiState,
    sheetState: ModalBottomSheetState,
    onEvent: (ExerciseListScreenEvent) -> Unit
) {
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            FilterSheet(
                uiState = uiState,
                onEvent = onEvent
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState.exerciseListState) {
                is UiState.DataState.Loading -> LoadingIndicator()
                else -> MainContent(
                    uiState = uiState,
                    onEvent = onEvent
                )
            }

            // Exercise Dialog
            if (uiState.showExerciseDialog) {
                ExerciseDialog(
                    onDismiss = {onEvent(ExerciseListScreenEvent.DismissAddExerciseDialog)},
                    onAdd = {onEvent(ExerciseListScreenEvent.AddWorkoutExercise(it))},
                    onEdit = {onEvent(ExerciseListScreenEvent.EditWorkoutExercise(it))},
                    selectedExercise = uiState.selectedExercise,
                    validationState = uiState.validationState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    uiState: UiState.WorkoutUiState,
    onEvent: (ExerciseListScreenEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            onNavigateBack = { onEvent(ExerciseListScreenEvent.NavigateBack) },
            onShowFilters = { onEvent(ExerciseListScreenEvent.ShowFilterSheet) },
            onSave = { onEvent(ExerciseListScreenEvent.SaveWorkout) }
        )

        CustomSearchBar(
            onQueryChange = { onEvent(ExerciseListScreenEvent.SearchQueryChanged(it)) },
            searchQuery = uiState.searchQuery,
        )

        ExerciseList(
            onExerciseAdd = { onEvent(ExerciseListScreenEvent.ExerciseSelected(it)) },
            onExerciseModify = { onEvent(ExerciseListScreenEvent.ExerciseModified(it)) },
            onExerciseDelete = { onEvent(ExerciseListScreenEvent.ExerciseDeleted(it)) },
            exerciseListState = uiState.exerciseListState,
            filteredExerciseList = uiState.filteredExerciseList,
            workoutExerciseList = uiState.workout.workoutExerciseList,
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onNavigateBack: () -> Unit,
    onShowFilters: () -> Unit,
    onSave: () -> Unit
) {
    TopAppBar(
        title = { Text("Workout Exercises") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onShowFilters) {
                Icon(Icons.Default.FilterList, "Filters")
            }
            IconButton(onClick = onSave) {
                Icon(Icons.Default.Save, "Save")
            }
        }
    )
}

@Composable
private fun ExerciseList(
    exerciseListState: UiState.DataState,
    filteredExerciseList: List<Exercise>,
    workoutExerciseList: List<WorkoutExercise>,
    onExerciseAdd: (Exercise) -> Unit,
    onExerciseModify: (WorkoutExercise) -> Unit,
    onExerciseDelete: (WorkoutExercise) -> Unit
) {
    when (exerciseListState) {
        is UiState.DataState.Success -> {
            if (filteredExerciseList.isNotEmpty()) {
                LazyColumn {
                    items(filteredExerciseList) { exercise ->
                        ExerciseListItem(
                            exercise = exercise,
                            workoutExercises = workoutExerciseList,
                            onAdd = onExerciseAdd,
                            onModify = onExerciseModify,
                            onDelete = onExerciseDelete
                        )
                    }
                }
            } else {
                EmptyComponent("No exercises found")
            }
        }
        is UiState.DataState.Error -> {
            ErrorComponent(text = (exerciseListState as UiState.DataState.Error).message)
        }
        else -> Unit
    }
}

@Composable
private fun ExerciseListItem(
    exercise: Exercise,
    workoutExercises: List<WorkoutExercise>,
    onAdd: (Exercise) -> Unit,
    onModify: (WorkoutExercise) -> Unit,
    onDelete: (WorkoutExercise) -> Unit
) {
    val workoutExercise = workoutExercises.find { it.exercise?.id == exercise.id }

    if (workoutExercise != null) {
        ExerciseListItem(
            type = ExerciseListItemType.WORKOUT_ADDED_TO_EXERCISE,
            workoutExercise = workoutExercise,
            onModify = { onModify(workoutExercise) },
            onDelete = { onDelete(workoutExercise) }
        )
    } else {
        ExerciseListItem(
            exercise = exercise,
            onAddToWorkout = { onAdd(exercise) },
            type = ExerciseListItemType.WORKOUT_NOT_ADDED_TO_EXERCISE
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDialog(
    selectedExercise: WorkoutExercise?,
    validationState: UiState.ValidationState,
    onDismiss: () -> Unit,
    onAdd: (WorkoutExercise?) -> Unit,
    onEdit: (WorkoutExercise?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ExerciseDialogContent(
            selectedExercise = selectedExercise,
            validationState = validationState,
            onDismiss = onDismiss,
            onAdd = onAdd,
            onEdit = onEdit
        )
    }
}


@Composable
fun FilterSheet(
    uiState: UiState.WorkoutUiState,
    onEvent: (ExerciseListScreenEvent) -> Unit,
) {

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Filter Search",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Body Part Dropdown
        ChipOptionList(
            label = "Body Part",
            options = uiState.bodyPartList,
            selectedOption = uiState.bodyPartFilter,
            onOptionSelected = { onEvent(ExerciseListScreenEvent.BodyPartFilterChange(it)) }
        )

        // Equipment Dropdown
        ChipOptionList(
            label = "Equipment",
            options =  uiState.equipmentList,
            selectedOption =  uiState.equipmentFilter,
            onOptionSelected = { onEvent(ExerciseListScreenEvent.EquipmentFilterChange(it)) }
        )

        // Target Dropdown
        ChipOptionList(
            label = "Target",
            options =  uiState.targetList,
            selectedOption =  uiState.targetFilter,
            onOptionSelected = { onEvent(ExerciseListScreenEvent.TargetMuscleFilterChange(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Apply Filters Button
        Button(
            onClick =  {
                onEvent(ExerciseListScreenEvent.ApplyFilters)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Apply Filters")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Clear Filters Button
        TextButton(
            onClick = {
                onEvent(ExerciseListScreenEvent.clearFilters)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Clear Filters")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDialogContent(
    selectedExercise: WorkoutExercise?,
    validationState: UiState.ValidationState,
    onDismiss: () -> Unit,
    onAdd: (WorkoutExercise?) -> Unit,
    onEdit: (WorkoutExercise?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exercise Name Header
            selectedExercise?.let { exercise ->
                exercise.exercise?.name?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Sets Input
            ExerciseNumberField(
                value = selectedExercise?.sets?.toString() ?: "",
                onValueChange = { setsString ->
                    val sets = setsString.toIntOrNull()
                    if (sets != null) {
                        onEdit(selectedExercise?.copy(sets = sets))
                    }
                },
                label = "Sets",
                error = validationState.setsError,
                modifier = Modifier.fillMaxWidth()
            )

            // Reps Input
            ExerciseNumberField(
                value = selectedExercise?.reps?.toString() ?: "",
                onValueChange = { repsString ->
                    val reps = repsString.toIntOrNull()
                    if (reps != null) {
                        onEdit(selectedExercise?.copy(reps = reps))
                    }
                },
                label = "Reps",
                error = validationState.repsError,
                modifier = Modifier.fillMaxWidth()
            )

            // Rest Input
            ExerciseNumberField(
                value = selectedExercise?.restBetweenSets?.toString() ?: "",
                onValueChange = { restString ->
                    val rest = restString.toIntOrNull()
                    if (rest != null) {
                        onEdit(selectedExercise?.copy(restBetweenSets = rest))
                    }
                },
                label = "Rest between sets (seconds)",
                error = validationState.restError,
                modifier = Modifier.fillMaxWidth()
            )

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onAdd(selectedExercise) },
                    enabled = selectedExercise != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Add")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                // Only allow digits
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    onValueChange(newValue)
                }
            },
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            isError = error != null,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                errorSupportingTextColor = MaterialTheme.colorScheme.error
            ),
            supportingText = error?.let {
                { Text(it) }
            }
        )
    }
}