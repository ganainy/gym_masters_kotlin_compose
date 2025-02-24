package com.ganainy.gymmasterscompose.ui.screens.create_workout.composables

import CustomSearchBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.shared_components.ChipOptionList
import com.ganainy.gymmasterscompose.ui.shared_components.EmptyComponent
import com.ganainy.gymmasterscompose.ui.shared_components.ExerciseListItem
import com.ganainy.gymmasterscompose.ui.shared_components.ExerciseListItemData
import com.ganainy.gymmasterscompose.ui.shared_components.ExerciseListItemType
import com.ganainy.gymmasterscompose.ui.models.BodyPart
import com.ganainy.gymmasterscompose.ui.models.Equipment
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.ui.screens.create_workout.CreateWorkoutAction
import com.ganainy.gymmasterscompose.ui.screens.create_workout.UiState
import com.ganainy.gymmasterscompose.utils.MockData.sampleBodyPartList
import com.ganainy.gymmasterscompose.utils.MockData.sampleEquipmentList
import com.ganainy.gymmasterscompose.utils.MockData.sampleExerciseListSmall
import com.ganainy.gymmasterscompose.utils.MockData.sampleTargetList
import com.ganainy.gymmasterscompose.utils.MockData.sampleWorkout
import com.ganainy.gymmasterscompose.utils.MockData.sampleWorkoutExercise
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkoutExerciseListScreenContent(
    uiState: UiState.WorkoutUiState,
    onEvent: (CreateWorkoutAction) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { true }
    )

    // Box for main content and dialogs
    Box(modifier = Modifier.fillMaxSize()) {
        ExerciseSelectionListForWorkoutComposable(
            uiState = uiState,
            onEvent = onEvent
        )

        // Exercise Dialog
        if (uiState.showExerciseDialog) {
            ExerciseDialog(
                onDismiss = { onEvent(CreateWorkoutAction.DismissAddExerciseDialog) },
                onAdd = { onEvent(CreateWorkoutAction.AddWorkoutExercise(it)) },
                onEdit = { onEvent(CreateWorkoutAction.EditWorkoutExercise(it)) },
                selectedExercise = uiState.selectedExercise,
                validationState = uiState.validationState
            )
        }
    }

    // ModalBottomSheet (conditionally rendered)
    if (uiState.showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    onEvent(CreateWorkoutAction.HideFilterSheet)
                }
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight(0.75f) // Limit sheet height for better UX
        ) {
            FilterSheet(
                bodyPartList = uiState.bodyPartList,
                bodyPartFilter = uiState.bodyPartFilter,
                equipmentList = uiState.equipmentList,
                equipmentFilter = uiState.equipmentFilter,
                targetList = uiState.targetList,
                targetFilter = uiState.targetFilter,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun ExerciseSelectionListForWorkoutComposable(
    uiState: UiState.WorkoutUiState,
    onEvent: (CreateWorkoutAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            onNavigateBack = { onEvent(CreateWorkoutAction.NavigateBack) },
            onShowFilters = { onEvent(CreateWorkoutAction.ShowFilterSheet) },
            onSave = { onEvent(CreateWorkoutAction.ToggleExerciseWorkoutListShow) }
        )

        CustomSearchBar(
            hint = stringResource(R.string.search_exercises),
            onQueryChange = { onEvent(CreateWorkoutAction.SearchQueryChanged(it)) },
            searchQuery = uiState.searchQuery,
        )

        ExerciseList(
            onExerciseAdd = { onEvent(CreateWorkoutAction.ExerciseSelected(it)) },
            onExerciseModify = { onEvent(CreateWorkoutAction.ExerciseModified(it)) },
            onExerciseDelete = { onEvent(CreateWorkoutAction.ExerciseDeleted(it)) },
            filteredExerciseList = uiState.filteredExerciseList,
            workoutExerciseList = uiState.workout.workoutExerciseList,
            onExerciseClick = {onEvent(CreateWorkoutAction.NavigateToExercise(it))},
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
fun ExerciseList(
    filteredExerciseList: List<Exercise>,
    workoutExerciseList: List<WorkoutExercise>,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseAdd: (Exercise) -> Unit,
    onExerciseModify: (WorkoutExercise) -> Unit,
    onExerciseDelete: (WorkoutExercise) -> Unit,
) {

            if (filteredExerciseList.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredExerciseList,
                        key = { exercise -> exercise.id } // Use exercise.id as key for performance
                    ) { exercise ->
                        val workoutExercise =
                            workoutExerciseList.find { it.exercise?.id == exercise.id }
                        if (workoutExercise != null) {
                            // WORKOUT_ADDED_TO_EXERCISE
                            ExerciseListItem(
                                data = ExerciseListItemData.WorkoutExerciseData(workoutExercise),
                                type = ExerciseListItemType.WORKOUT_ADDED_TO_EXERCISE,
                                onClick = { onExerciseClick(exercise) },
                                onAddToWorkout = { onExerciseAdd(exercise) },
                                onModify = { onExerciseModify(workoutExercise) },
                                onDelete = { onExerciseDelete(workoutExercise) }
                            )
                        } else {
                            // WORKOUT_NOT_ADDED_TO_EXERCISE
                            ExerciseListItem(
                                data = ExerciseListItemData.ExerciseData(exercise),
                                type = ExerciseListItemType.WORKOUT_NOT_ADDED_TO_EXERCISE,
                                onClick = { onExerciseClick(exercise) },
                                onAddToWorkout = { onExerciseAdd(exercise) },
                                onModify = { /* No-op for this type */ },
                                onDelete = { /* No-op for this type */ }
                            )
                        }
                    }
                }
            } else {
                EmptyComponent(text = "No exercises found")
            }
        }




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

@Preview(showBackground = true)
@Composable
fun ExerciseDialogPreview() {
    AppTheme {
    ExerciseDialog(
        selectedExercise = sampleWorkoutExercise,
        validationState = UiState.ValidationState(), // Provide a sample or mock ValidationState if needed
        onDismiss = {},
        onAdd = {},
        onEdit = {}
    )
} }

@Composable
fun FilterSheet(
    bodyPartList: List<BodyPart>,
    bodyPartFilter: BodyPart?,
    equipmentList: List<Equipment>,
    equipmentFilter: Equipment?,
    targetList: List<TargetMuscle>,
    targetFilter: TargetMuscle?,
    onEvent: (CreateWorkoutAction) -> Unit,
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
            options = bodyPartList,
            selectedOption = bodyPartFilter,
            onOptionSelected = { onEvent(CreateWorkoutAction.BodyPartFilterChange(it)) }
        )

        // Equipment Dropdown
        ChipOptionList(
            label = "Equipment",
            options = equipmentList,
            selectedOption = equipmentFilter,
            onOptionSelected = { onEvent(CreateWorkoutAction.EquipmentFilterChange(it)) }
        )

        // Target Dropdown
        ChipOptionList(
            label = "Target",
            options = targetList,
            selectedOption = targetFilter,
            onOptionSelected = { onEvent(CreateWorkoutAction.TargetMuscleFilterChange(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Apply Filters Button
        Button(
            onClick = {
                onEvent(CreateWorkoutAction.ApplyFilters)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Apply Filters")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Clear Filters Button
        TextButton(
            onClick = {
                onEvent(CreateWorkoutAction.ClearFilters)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Clear Filters")
        }
    }
}

@Preview(showBackground = true, name = "Filter Sheet Preview")
@Composable
fun FilterSheetPreview() {
    AppTheme {
    FilterSheet(
        bodyPartList = sampleBodyPartList,
        bodyPartFilter = sampleBodyPartList.first(),
        equipmentList = sampleEquipmentList,
        equipmentFilter = sampleEquipmentList.first(),
        targetList = sampleTargetList,
        targetFilter = sampleTargetList.first(),
        onEvent = { /* No-op for preview */ }
    )
}
}

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
            supportingText = error?.let {
                { Text(it) }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun WorkoutExerciseListScreenPreview() {
    AppTheme {
        WorkoutExerciseListScreenContent(
            uiState = UiState.WorkoutUiState(
                workoutState = UiState.DataState.ExerciseListSetup,
                filteredExerciseList = sampleExerciseListSmall,
                searchQuery = "",
                showExerciseDialog = false,
                selectedExercise = null,
                validationState = UiState.ValidationState(),    // No validation errors
                workout = sampleWorkout,

            ),
            onEvent = {},    // No-op
        )
    }
}
