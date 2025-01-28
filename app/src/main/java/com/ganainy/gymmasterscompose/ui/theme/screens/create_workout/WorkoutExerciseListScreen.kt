package com.ganainy.gymmasterscompose.ui.theme.screens.create_workout

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
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
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
import com.ganainy.gymmasterscompose.ui.theme.models.WorkoutExercise
import com.ganainy.gymmasterscompose.utils.Utils.showToast
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutExerciseListScreen(
    navigateBack: () -> Unit,
    viewModel: CreateWorkoutViewModel
) {

    var showExerciseDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    // State for controlling the bottom sheet visibility
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            FilterSearchUI(
                bodyPartList = uiState.bodyPartList,
                equipmentList = uiState.equipmentList,
                targetList = uiState.targetList,
                bodyPartFilter = uiState.bodyPartFilter,
                onBodyPartFilterChange = viewModel::onBodyPartFilterChange,
                equipmentFilter = uiState.equipmentFilter,
                onEquipmentFilterChange = viewModel::onEquipmentFilterChange,
                targetFilter = uiState.targetFilter,
                onTargetMuscleFilterChange = viewModel::onTargetMuscleFilterChange,
                onApplyFilters = {
                    viewModel.applyFilters()
                    coroutineScope.launch {
                        sheetState.hide()
                    }
                }
            )
        }
    ) {

        when (uiState.exerciseListState) {

            is ExerciseListDataState.Initial -> {
                // Do nothing
            }

            is ExerciseListDataState.Loading -> {
                LoadingIndicator()
            }

            is ExerciseListDataState.Success -> {
                // Do nothing
            }

            is ExerciseListDataState.Error -> {
                showToast(
                    context,
                    (uiState.exerciseListState as ExerciseListDataState.Error).message,
                )
                viewModel.clearError()
            }
        }

        // Main Screen Content
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ExercisesContent(
                uiState = uiState,
                navigateBack,
                showModalSheet = {
                    coroutineScope.launch {
                        sheetState.show()
                    }
                },
                searchQuery = uiState.searchQuery,
                onQueryChange = viewModel::onQueryChange,
                onAddExerciseClick = {
                    viewModel.setSelectedExercise(it)
                    showExerciseDialog = true
                },
                onModifyExerciseClick = {
                    viewModel.setSelectedExercise(it.exercise)
                    showExerciseDialog = true
                },
                onDeleteExerciseClick = viewModel::deleteWorkoutExercise,
            )

        }



        if (showExerciseDialog) {
            AddExerciseDialog(
                selectedExercise = uiState.selectedExercise,
                onDismiss = { showExerciseDialog = false },
                onExerciseAdd = { newWorkoutExercise ->
                    viewModel.addWorkoutExercise(newWorkoutExercise)
                    showExerciseDialog = false
                },
                onEditWorkoutExercise = viewModel::editWorkoutExercise,
            )
        }


    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ExercisesContent(
    uiState: WorkoutUiState,
    navigateBack: () -> Unit,
    showModalSheet: () -> Job,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onAddExerciseClick: (Exercise) -> Unit,
    onModifyExerciseClick: (WorkoutExercise) -> Unit,
    onDeleteExerciseClick: (WorkoutExercise) -> Unit,
) {
    val filteredExercises = uiState.filteredExerciseList


    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Workout Exercises") },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { showModalSheet() }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters"
                    )
                }
                IconButton(onClick = navigateBack) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save"
                    )
                }
            }
        )

        SearchBar(
            searchQuery = searchQuery,
            onQueryChange = onQueryChange,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )


        when (uiState.exerciseListState) {

            is ExerciseListDataState.Initial -> {
                // Do nothing
            }

            is ExerciseListDataState.Loading -> {
                LoadingIndicator()
            }

            is ExerciseListDataState.Success -> {
                if (filteredExercises.isNotEmpty()) {
                    LazyColumn {
                        items(filteredExercises) { exercise ->
                            val workoutExerciseList: List<WorkoutExercise> =
                                uiState.workoutExerciseList
                            if (workoutExerciseList.any { it.exercise.id == exercise.id }) {
                                //exercise has been already added to workout
                                val workoutExercise =
                                    workoutExerciseList.find { it.exercise.id == exercise.id }
                                ExerciseListItem(
                                    type = ExerciseListItemType.WORKOUT_ADDED_TO_EXERCISE,
                                    workoutExercise = workoutExercise,
                                    onModify = {
                                        if (workoutExercise != null) {
                                            onModifyExerciseClick(workoutExercise)
                                        }
                                    },
                                    onDelete = {
                                        if (workoutExercise != null) {
                                            onDeleteExerciseClick(workoutExercise)
                                        }
                                    },
                                )
                            } else {
                                //exercise has not been added to workout
                                ExerciseListItem(
                                    exercise = exercise,
                                    onAddToWorkout = { onAddExerciseClick(exercise) },
                                    type = ExerciseListItemType.WORKOUT_NOT_ADDED_TO_EXERCISE,
                                )
                            }
                        }
                    }
                } else {
                    EmptyComponent("No exercises found")
                }
            }

            is ExerciseListDataState.Error -> {
                ErrorComponent(text = (uiState.exerciseListState as ExerciseListDataState.Error).message)
            }
        }


    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = { onQueryChange(it) },
        modifier = modifier,
        placeholder = { Text("Search exercise") },
        leadingIcon = { Icon(Icons.Default.Search, "Search") },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseDialog(
    selectedExercise: WorkoutExercise?,
    onDismiss: () -> Unit,
    onExerciseAdd: (WorkoutExercise?) -> Unit,
    onEditWorkoutExercise: (WorkoutExercise?) -> Unit
) {


    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedExercise != null) {
                    Text(
                        selectedExercise.exercise.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                OutlinedTextField(
                    value = selectedExercise?.sets?.toString() ?: "",
                    onValueChange = { setsString: String ->
                        val sets = setsString.toIntOrNull()
                        if (sets != null) {
                            onEditWorkoutExercise(selectedExercise?.copy(sets = sets))
                        }
                    },
                    label = { Text("Sets") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = selectedExercise?.reps?.toString() ?: "",
                    onValueChange = { reps: String ->
                        val repsInt = reps.toIntOrNull()
                        if (repsInt != null) {
                            onEditWorkoutExercise(selectedExercise?.copy(reps = repsInt))
                        }
                    },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )


                OutlinedTextField(
                    value = selectedExercise?.restBetweenSets?.toString() ?: "",
                    onValueChange = { rest: String ->
                        val restInt = rest.toIntOrNull()
                        if (restInt != null) {
                            onEditWorkoutExercise(
                                selectedExercise?.copy(
                                    restBetweenSets = restInt
                                )
                            )
                        }
                    },
                    label = { Text("Rest between sets (seconds)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Button(
                        onClick = { onExerciseAdd(selectedExercise) },
                        enabled = selectedExercise != null
                    ) {
                        Text("Add to workout")
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSearchUI(
    bodyPartList: List<BodyPart>?,
    equipmentList: List<Equipment>?,
    targetList: List<TargetMuscle>?,
    bodyPartFilter: BodyPart?,
    onBodyPartFilterChange: (BodyPart?) -> Unit,
    equipmentFilter: Equipment?,
    onEquipmentFilterChange: (Equipment?) -> Unit,
    targetFilter: TargetMuscle?,
    onTargetMuscleFilterChange: (TargetMuscle?) -> Unit,
    onApplyFilters: () -> Unit
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
            onOptionSelected = { onBodyPartFilterChange(it) }
        )

        // Equipment Dropdown
        ChipOptionList(
            label = "Equipment",
            options = equipmentList,
            selectedOption = equipmentFilter,
            onOptionSelected = { onEquipmentFilterChange(it) }
        )

        // Target Dropdown
        ChipOptionList(
            label = "Target",
            options = targetList,
            selectedOption = targetFilter,
            onOptionSelected = { onTargetMuscleFilterChange(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Apply Filters Button
        Button(
            onClick = onApplyFilters,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Apply Filters")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Clear Filters Button
        TextButton(
            onClick = {
                onBodyPartFilterChange(null)
                onEquipmentFilterChange(null)
                onTargetMuscleFilterChange(null)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Clear Filters")
        }
    }
}


@Preview
@Composable
fun PreviewFilterSearchUI() {
    val bodyPartList = listOf("Back", "Cardio", "Chest", "Lower Arms")
    val equipmentList = listOf("Assisted", "Band", "Barbell", "Body Weight")
    val targetList = listOf("Abductors", "Abs", "Adductors", "Biceps", "Calves")

    FilterSearchUI(
        bodyPartList = listOf(
            BodyPart("Back"),
            BodyPart("Cardio"),
            BodyPart("Chest"),
            BodyPart("Lower Arms")
        ),
        equipmentList = listOf(
            Equipment("Assisted"),
            Equipment("Band"),
            Equipment("Barbell"),
            Equipment("Body Weight")
        ),
        targetList = listOf(
            TargetMuscle("Abductors"),
            TargetMuscle("Abs"),
            TargetMuscle("Adductors"),
            TargetMuscle("Biceps"),
            TargetMuscle("Calves")
        ),
        bodyPartFilter = null,
        onBodyPartFilterChange = { /* Handle body part filter change */ },
        equipmentFilter = null,
        onEquipmentFilterChange = { /* Handle equipment filter change */ },
        targetFilter = null,
        onTargetMuscleFilterChange = { /* Handle target muscle filter change */ },
        onApplyFilters = { /* Handle apply filters */ }
    )
}
