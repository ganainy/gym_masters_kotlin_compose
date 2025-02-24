package com.ganainy.gymmasterscompose.ui.screens.workout_list

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.shared_components.CustomTopAppBar
import com.ganainy.gymmasterscompose.ui.shared_components.LoadingIndicator
import com.ganainy.gymmasterscompose.utils.MockData

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutListContent(
    uiData: WorkoutListUiData,
    onAction: (WorkoutListScreenAction) -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = stringResource(R.string.workouts),
                         )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search and Filter Section
            SearchBar(
                searchQuery = uiData.searchQuery,
                onQueryChange = {
                    onAction(WorkoutListScreenAction.OnQueryChange(it))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .animateContentSize(),
                filterOptionList = SortType.entries.map { it.name },
                selectedFilterOption = uiData.sortType.name,
                onOptionSelected = {
                    onAction(WorkoutListScreenAction.OnSortPreferenceSelected(SortType.valueOf(it)))
                },
                shape = RoundedCornerShape(12.dp),
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    inputFieldColors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            )

            // Content Section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Crossfade(
                    targetState = uiData.isLoading,
                    animationSpec = tween(300)
                ) { isLoading ->
                    when {
                        isLoading -> {
                            LoadingIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        uiData.error != null -> {
                            ErrorContent(
                                message = uiData.error,
                                onRetry = {
                                    onAction(WorkoutListScreenAction.OnRetry)
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp)
                            )
                        }
                        else -> {
                            when {
                                uiData.workoutWithStatusList.isEmpty() -> {
                                    EmptyContent(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(16.dp)
                                    )
                                }
                                else -> {
                                    WorkoutList(
                                        workoutList = uiData.workoutWithStatusList,
                                        onWorkoutClick = { workout, isLiked, isSaved ->
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
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkoutList(
    workoutList: List<WorkoutWithStatus>,
    onWorkoutClick: (Workout, Boolean, Boolean) -> Unit,
    onWorkoutLike: (Workout) -> Unit,
    onWorkoutSave: (Workout) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = workoutList,
            key = { it.workout.id }
        ) { workout ->
            WorkoutPreviewComposable(
                workoutWithStatus = workout,
                previewOnlyParams = PreviewOnlyParams(
                    onWorkoutLike = onWorkoutLike,
                    onWorkoutSave = onWorkoutSave,
                    onWorkoutClick = onWorkoutClick,
                ),
                modifier = Modifier
                    .animateItemPlacement()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        onWorkoutClick(
                            workout.workout,
                            workout.isLiked,
                            workout.isSaved
                        )
                    }
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String, onRetry: () -> Unit, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
private fun EmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
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
    onOptionSelected: (String) -> Unit,
    shape: RoundedCornerShape,
    colors: SearchBarColors

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
            workoutWithStatusList = listOf(
                WorkoutWithStatus(
                    workout = Workout(
                        id = "1",
                        title = "Workout 1",
                        description = "This is a workout",
                        imageUrl = "https://firebasestorage.googleapis.com/v0/b/gym-masters.appspot.com/o/workouts%2F-Myv9QdZ0nK7WYV3JpQJk%2Fimage?alt=media&token=0e0f9f5b-9d3e-4e1e-94f1-6c9e0d7a5f0c",
                        workoutExerciseList = MockData.sampleWorkoutExerciseList
                    ),
                    isLiked = false,
                    isSaved = false
                ),
                WorkoutWithStatus(
                    workout = Workout(
                        id = "2",
                        title = "Workout 2",
                        description = "This is another workout",
                        imageUrl = "",
                        workoutExerciseList = emptyList()
                    ),
                    isLiked = false,
                    isSaved = false
                )
            ),
            isLoading = false,
            error = null,
            searchQuery = ""
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



