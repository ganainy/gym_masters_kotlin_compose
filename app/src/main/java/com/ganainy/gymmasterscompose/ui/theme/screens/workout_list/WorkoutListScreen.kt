package com.ganainy.gymmasterscompose.ui.theme.screens.workout_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ganainy.gymmasterscompose.ui.theme.components.LoadingIndicator
import com.ganainy.gymmasterscompose.ui.theme.components.WorkoutCard
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(navController: NavHostController) {

    val navigateToWorkout: (Workout) -> Unit = { workout ->
        //todo
        //navController.navigate(Screen.Main.Profile.route + "/$userId")
    }


    val viewModel: WorkoutListViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()


    WorkoutListContent(
        uiState = uiState,
        onQueryChange = viewModel::updateSearchQuery,
        onSortPreferenceSelected = viewModel::updateSortPreference,
        onWorkoutClick = navigateToWorkout,
        onRetry = viewModel::retry,
        onWorkoutLike = viewModel::toggleWorkoutLike,
        onWorkoutSave = viewModel::toggleWorkoutSave,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutListContent(
    uiState: WorkoutListUiData,
    onSortPreferenceSelected: (SortType) -> Unit,
    onQueryChange: (String) -> Unit,
    onWorkoutClick: (Workout) -> Unit,
    onWorkoutLike: (Workout) -> Unit,
    onWorkoutSave: (Workout) -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        /* topBar = {
             TopAppBar(
                 title = { Text("Exercises") },
                 actions = {
                     TextButton(onClick = onShowFilters) {
                         Text("Filters", color = MaterialTheme.colorScheme.primary)
                     }
                 }
             )
         }*/
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                searchQuery = uiState.searchQuery,
                onQueryChange = onQueryChange,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )

            if (uiState.isLoading) {
                LoadingIndicator()
            }
            if (uiState.error != null) {
                ErrorContent(
                    message = uiState.error,
                    onRetry = onRetry
                )
            } else {
                if (uiState.workoutWithStatusList.isEmpty()) {
                    EmptyContent(
                    )
                } else {
                    WorkoutList(
                        workoutList = uiState.workoutWithStatusList,
                        onWorkoutClick =onWorkoutClick ,
                        onWorkoutLike = onWorkoutLike,
                        onWorkoutSave =onWorkoutSave ,
                    )
                }


            }

        }
    }
}

@Composable
private fun WorkoutList(
    workoutList: List<WorkoutWithStatus>,
    onWorkoutClick: (Workout) -> Unit,
    onWorkoutLike: (Workout) -> Unit,
    onWorkoutSave: (Workout) -> Unit
) {
    LazyColumn {
        items(workoutList,
            key = { it.workout.id }) { workout ->
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
private fun ErrorContent(
    message: String,
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
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyContent() {
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
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search workouts") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}