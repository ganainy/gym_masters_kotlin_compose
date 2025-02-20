package com.ganainy.gymmasterscompose.ui.theme.screens.workout_list

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.ui.theme.components.LoadingIndicator
import com.ganainy.gymmasterscompose.ui.theme.components.WorkoutCard
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout

@Composable
fun WorkoutListScreen(
    navigateToWorkoutDetails: (Workout, isLiked: Boolean, isSaved: Boolean) -> Unit
) {


    val viewModel: WorkoutListViewModel = hiltViewModel()

    val uiData by viewModel.uiData.collectAsState()

    WorkoutListContent(
        uiData = uiData,
        onAction = { action ->
            when (action) {
                is WorkoutListScreenAction.OnSortPreferenceSelected -> viewModel.updateSortPreference(
                    action.sortType
                )

                is WorkoutListScreenAction.OnQueryChange -> viewModel.updateSearchQuery(action.query)
                is WorkoutListScreenAction.OnWorkoutClick -> navigateToWorkoutDetails(
                    action.workout,
                    action.isLiked,
                    action.isSaved
                )

                is WorkoutListScreenAction.OnWorkoutLike -> viewModel.toggleWorkoutLike(action.workout)
                is WorkoutListScreenAction.OnWorkoutSave -> viewModel.toggleWorkoutSave(action.workout)
                WorkoutListScreenAction.OnRetry -> viewModel.retry()
            }
        },
    )
}

@Composable
private fun WorkoutListContent(
    uiData: WorkoutListUiData,
    onAction: (WorkoutListScreenAction) -> Unit
) {
    Scaffold(

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(searchQuery = uiData.searchQuery,
                onQueryChange = {onAction(WorkoutListScreenAction.OnQueryChange(it))},
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                filterOptionList = SortType.entries.map { it.name },
                selectedFilterOption = uiData.sortType.name,
                onOptionSelected = { onAction(WorkoutListScreenAction.OnSortPreferenceSelected(SortType.valueOf(it))) }

            )

            if (uiData.isLoading) {
                LoadingIndicator()
            }
            if (uiData.error != null) {
                ErrorContent(
                    message = uiData.error, onRetry = {
                        onAction(WorkoutListScreenAction.OnRetry)
                    }
                )
            } else {
                if (uiData.workoutWithStatusList.isEmpty()) {
                    EmptyContent(
                    )
                } else {
                    WorkoutList(
                        workoutList = uiData.workoutWithStatusList,
                        onWorkoutClick = { workout,isLiked,isSaved ->
                            onAction(
                                WorkoutListScreenAction.OnWorkoutClick(
                                    workout = workout,
                                    isLiked = isLiked,
                                    isSaved = isSaved
                                )
                            )
                        },
                        onWorkoutLike = {
                            onAction(WorkoutListScreenAction.OnWorkoutLike(it))
                        },
                        onWorkoutSave = {
                            onAction(WorkoutListScreenAction.OnWorkoutSave(it))
                        },
                    )
                }


            }

        }
    }
}

@Composable
private fun WorkoutList(
    workoutList: List<WorkoutWithStatus>,
    onWorkoutClick: (Workout, Boolean, Boolean) -> Unit,
    onWorkoutLike: (Workout) -> Unit,
    onWorkoutSave: (Workout) -> Unit
) {
    LazyColumn {
        items(workoutList, key = { it.workout.id }) { workout ->
            WorkoutCard(
                workoutWithStatus = workout,
                onWorkoutClick = onWorkoutClick,
                onWorkoutLike = onWorkoutLike,
                onWorkoutSave = onWorkoutSave,
            )
        }
    }
}


@Composable
fun ErrorContent(
    message: String, onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "No workouts found",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    filterOptionList: List<String>,
    selectedFilterOption: String,
    onOptionSelected: (String) -> Unit

) {
    Row(
        modifier = modifier, verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search workouts") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilterMenu(
            options = filterOptionList,
            selectedOption = selectedFilterOption,
            onOptionSelected = onOptionSelected
        )
    }
}


@Composable
fun FilterMenu(
    options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onOptionSelected(option)
                    expanded = false
                })
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewWorkoutListScreen() {
    WorkoutListContent(
        uiData = WorkoutListUiData(
            workoutWithStatusList = List(2) { index ->
                WorkoutWithStatus(
                    workout = Workout(
                        id = index.toString(),
                        title = "Workout $index",
                        description = "This is a workout",
                        imageUrl = "",
                        workoutExerciseList = emptyList()
                    ),
                    isLiked = false,
                    isSaved = false
                )
            }
        ),
        onAction = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewWorkoutListEmptyScreen() {
    WorkoutListContent(
        uiData = WorkoutListUiData(
            workoutWithStatusList = emptyList(),
            isLoading = false,
            error = null,
            searchQuery = "",
            sortType = SortType.NEWEST
        ),
        onAction = {}
    )
}



