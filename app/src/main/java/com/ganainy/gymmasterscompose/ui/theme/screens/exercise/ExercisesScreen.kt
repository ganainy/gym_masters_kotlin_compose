package com.ganainy.gymmasterscompose.ui.theme.screens.exercise

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.R

data class Exercise(
    val name: String,
    val muscle: String,
    val iconResId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen() {
    val exercises = listOf(
        Exercise("Squat (Machine)", "Quadriceps", R.drawable.squat_machine),
        Exercise("Lying Leg Curl (Machine)", "Hamstrings", R.drawable.leg_curl),
        Exercise("Leg Extension (Machine)", "Quadriceps", R.drawable.leg_extension),
        Exercise("Leg Press (Machine)", "Quadriceps", R.drawable.leg_press),
        Exercise("Single Leg Standing Calf Raise (Machine)", "Calves", R.drawable.calf_raise),
        Exercise("Hip Abduction (Machine)", "Abductors", R.drawable.hip_abduction),
        Exercise("Lat Pulldown (Cable)", "Lats", R.drawable.lat_pulldown)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Exercises") },
            navigationIcon = {
                IconButton(onClick = { /* Handle navigation */ }) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            actions = {
                TextButton(onClick = { /* Handle create */ }) {
                    Text("Create", color = MaterialTheme.colorScheme.primary)
                }
            }
        )

        SearchBar(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        )

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = true,
                onClick = { /* Handle filter */ },
                label = { Text("All Equipment") }
            )
            FilterChip(
                selected = false,
                onClick = { /* Handle filter */ },
                label = { Text("All Muscles") }
            )
        }

        Text(
            "Recent Exercises",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn {
            items(exercises) { exercise ->
                ExerciseItem(exercise)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = "",
        onValueChange = { /* Handle search */ },
        modifier = modifier,
        placeholder = { Text("Search exercise") },
        leadingIcon = { Icon(Icons.Default.Search, "Search") },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun ExerciseItem(exercise: Exercise) {
    ListItem(
        headlineContent = { Text(exercise.name) },
        supportingContent = { Text(exercise.muscle) },
        leadingContent = {
            Image(
                painter = painterResource(exercise.iconResId),
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, "View details")
        },
        modifier = Modifier.clickable { /* Handle click */ }
    )
}