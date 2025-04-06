package com.ganainy.gymmasterscompose.ui.screens.exercise_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.ui.models.BodyPart
import com.ganainy.gymmasterscompose.ui.models.Equipment
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.shared_components.CustomTopAppBar
import com.ganainy.gymmasterscompose.ui.shared_components.ExerciseListItem
import com.ganainy.gymmasterscompose.ui.shared_components.ExerciseListItemData
import com.ganainy.gymmasterscompose.ui.shared_components.ExerciseListItemType
import com.ganainy.gymmasterscompose.ui.shared_components.LoadingIndicator
import kotlinx.coroutines.launch
//todo fix downloading all exercises on first startup takes too long (at least show progress and make cancelable)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    navigateToExercise: (Exercise) -> Unit,
    navigateBack: () -> Unit,
    viewModel: ExerciseListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredExercises by viewModel.filteredExercises.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    var showBottomSheet by remember { mutableStateOf(false) }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            FilterSearchUI(
                uiState = uiState,
                onUpdateFilters = viewModel::updateFilters,
                onFiltersCleared = {
                    viewModel.clearFilters()
                    coroutineScope.launch { sheetState.hide() }
                    showBottomSheet = false
                }
            )
        }
    }

    ExercisesContent(
        uiState = uiState,
        filteredExercises = filteredExercises,
        onNavigateBack = navigateBack,
        onShowFilters = { showBottomSheet = true },
        onQueryChange = viewModel::updateSearchQuery,
        onExerciseClick = navigateToExercise,
        onRetry = viewModel::retry
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisesContent(
    uiState: ExerciseListUiState,
    filteredExercises: List<Exercise>,
    onNavigateBack: () -> Unit,
    onShowFilters: () -> Unit,
    onQueryChange: (String) -> Unit,
    onExerciseClick: (Exercise) -> Unit,
    onRetry: () -> Unit
) {

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {


            CustomTopAppBar(
                title =  "Exercises",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onNavigateBack,
                actionIcons = listOf(Icons.Default.FilterList),
                onActionClicks = listOf { onShowFilters() },

            )


            SearchBar(
                searchQuery = uiState.searchQuery,
                onQueryChange = onQueryChange,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )

            when (uiState.dataState) {
                is DataState.Loading -> LoadingIndicator() // Show loading indicator

                is DataState.Error -> ErrorContent( // Error loading *cached* data
                    message = uiState.dataState.message,
                    showRetryButton = false, //  don't show retry for cache errors
                    onRetry = onRetry,
                )

                is DataState.InitialDownloadRequired -> InitialDownloadContent(
                    message = uiState.dataState.message,
                    onRetry = onRetry // Use the same retry function (VM logic handles it)
                )

                is DataState.Success -> { // Success - display list or empty message
                    if (filteredExercises.isEmpty()) {
                        EmptyContent(
                            hasActiveFilters = uiState.filters.activeFilters != ActiveFilters()
                        )
                    } else {
                        ExerciseList(
                            exercises = filteredExercises,
                            onExerciseClick = onExerciseClick,
                        )
                    }
                }
            }
        }
}

@Composable
fun ExerciseList(
    exercises: List<Exercise>,
    onExerciseClick: (Exercise) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = exercises,
            key = { it.id } // Use exercise.id as key for performance
        ) { exercise ->
            ExerciseListItem(
                data = ExerciseListItemData.ExerciseData(exercise),
                type = ExerciseListItemType.EXERCISE,
                onClick = onExerciseClick,
                onAddToWorkout = { /* No-op for EXERCISE type */ },
                onModify = { /* No-op for EXERCISE type */ },
                onDelete = { /* No-op for EXERCISE type */ }
            )
            if (exercises.last() != exercise) {
                HorizontalDivider()
        }
        }
    }
}

@Composable
private fun FilterSearchUI(
    uiState: ExerciseListUiState,
    onUpdateFilters: (ActiveFilters) -> Unit,
    onFiltersCleared: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Filter Exercises",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        FilterSection(
            title = "Body Part",
            options = uiState.filters.bodyPartList,
            selectedOption = uiState.filters.activeFilters.bodyPart,
            onOptionSelected = { bodyPart ->
                onUpdateFilters(uiState.filters.activeFilters.copy(bodyPart = bodyPart))
            }
        )

        FilterSection(
            title = "Equipment",
            options = uiState.filters.equipmentList,
            selectedOption = uiState.filters.activeFilters.equipment,
            onOptionSelected = { equipment ->
                onUpdateFilters(uiState.filters.activeFilters.copy(equipment = equipment))
            }
        )

        FilterSection(
            title = "Target Muscle",
            options = uiState.filters.targetList,
            selectedOption = uiState.filters.activeFilters.targetMuscle,
            onOptionSelected = { target ->
                onUpdateFilters(uiState.filters.activeFilters.copy(targetMuscle = target))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onFiltersCleared,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear Filters")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> FilterSection(
    title: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selectedOption,
                    onClick = {
                        if (option == selectedOption) {
                            onOptionSelected(null)
                        } else {
                            onOptionSelected(option)
                        }
                    },
                    label = {
                        Text(
                            when (option) {
                                is BodyPart -> option.name
                                is Equipment -> option.name
                                is TargetMuscle -> option.name
                                else -> option.toString()
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    showRetryButton: Boolean = true,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (showRetryButton) {
            Button(onClick = onRetry) {
            Text("Retry")
        }
        }
    }
}

@Composable
private fun InitialDownloadContent(
    message: String,
    onRetry: () -> Unit // This triggers the download via VM.retry()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "An internet connection is required for the initial download.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            // Button text can indicate download action
            Text("Download Exercises")
        }
    }
}

@Composable
private fun EmptyContent(hasActiveFilters: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (hasActiveFilters) {
                "No exercises match your filters"
            } else {
                "No exercises found"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
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
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search exercises") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        singleLine = true,
    )
}