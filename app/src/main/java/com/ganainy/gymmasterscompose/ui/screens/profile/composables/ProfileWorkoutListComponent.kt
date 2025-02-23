package com.ganainy.gymmasterscompose.ui.screens.profile.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.ui.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.screens.profile.EmptyStateActionType
import com.ganainy.gymmasterscompose.ui.screens.workout_list.WorkoutWithStatus

@Composable
internal fun ProfileWorkoutListComponent(
    workouts: List<WorkoutWithStatus>,
    onAddWorkoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOwnProfile: Boolean = false,
    onWorkoutClick: (WorkoutWithStatus) -> Unit,
) {
    if (workouts.isEmpty()) {
        EmptyStateComponent(
            isOwnProfile = isOwnProfile,
            onAction = onAddWorkoutClick,
            actionType = EmptyStateActionType.CREATE_WORKOUT_ACTION,
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        ) {
            items(
                items = workouts,
                key = { it.workout.id }
            ) { workoutWithStatus ->
                WorkoutListItem(
                    workoutWithStatus = workoutWithStatus,
                    onWorkoutClick = onWorkoutClick,
                )
                if (workoutWithStatus != workouts.last()) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun WorkoutListItem(
    workoutWithStatus: WorkoutWithStatus,
    onWorkoutClick: (WorkoutWithStatus) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWorkoutClick(workoutWithStatus) }
            .padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // Workout title
        Text(
            text = workoutWithStatus.workout.title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Workout description or details
        Text(
            text = workoutWithStatus.workout.description ?: "No description",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Workout status or metrics (e.g., duration, exercises count)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Duration",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${workoutWithStatus.workout.workoutDuration.takeIf { it.isNotEmpty() } ?: "N/A"} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Exercises",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${workoutWithStatus.workout.workoutExerciseList.size} exercises",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileWorkoutListComponent() {
    ProfileWorkoutListComponent(
        workouts = listOf(
            WorkoutWithStatus(
                workout = Workout(
                    id = "1",
                    title = "Workout 1",
                    description = "This is a workout"
                ),
                isLiked = false,
                isSaved = false
            ),
            WorkoutWithStatus(
                workout = Workout(
                    id = "2",
                    title = "Workout 2",
                    description = "This is another workout"
                ),
                isLiked = false,
                isSaved = false
            )
        ),
        onAddWorkoutClick = {},
        isOwnProfile = true,
        onWorkoutClick = {}
    )
}
