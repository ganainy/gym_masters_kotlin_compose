package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.AppTheme
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.utils.MockData.sampleExercise
import com.ganainy.gymmasterscompose.utils.MockData.sampleWorkoutExercise

// Enum for item types
enum class ExerciseListItemType {
    EXERCISE,
    WORKOUT_ADDED_TO_EXERCISE,
    WORKOUT_NOT_ADDED_TO_EXERCISE
}

// Sealed class to encapsulate exercise or workout exercise data
sealed class ExerciseListItemData {
    data class ExerciseData(val exercise: Exercise) : ExerciseListItemData()
    data class WorkoutExerciseData(val workoutExercise: WorkoutExercise) : ExerciseListItemData()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseListItem(
    data: ExerciseListItemData,
    type: ExerciseListItemType,
    onClick: (Exercise) -> Unit,
    onAddToWorkout: () -> Unit = {},
    onModify: (WorkoutExercise) -> Unit = {},
    onDelete: (WorkoutExercise) -> Unit = {},
) {

    ListItem(
        headlineContent = {
            Text(
                text = when (data) {
                    is ExerciseListItemData.ExerciseData -> data.exercise.name
                    is ExerciseListItemData.WorkoutExerciseData -> data.workoutExercise.exercise?.name.orEmpty()
                }
            )
        },
        supportingContent = {
            Column {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val bodyPart = when (data) {
                        is ExerciseListItemData.ExerciseData -> data.exercise.bodyPart
                        is ExerciseListItemData.WorkoutExerciseData -> data.workoutExercise.exercise?.bodyPart.orEmpty()
                    }
                    val equipment = when (data) {
                        is ExerciseListItemData.ExerciseData -> data.exercise.equipment
                        is ExerciseListItemData.WorkoutExerciseData -> data.workoutExercise.exercise?.equipment.orEmpty()
                    }
                    val target = when (data) {
                        is ExerciseListItemData.ExerciseData -> data.exercise.target
                        is ExerciseListItemData.WorkoutExerciseData -> data.workoutExercise.exercise?.target.orEmpty()
                    }

                    if (bodyPart.isNotEmpty()) CustomChip(bodyPart)
                    if (equipment.isNotEmpty()) CustomChip(equipment)
                    if (target.isNotEmpty()) CustomChip(target)
                }

                if (type == ExerciseListItemType.WORKOUT_ADDED_TO_EXERCISE && data is ExerciseListItemData.WorkoutExerciseData) {
                    WorkoutDetails(workoutExercise = data.workoutExercise)
                }
            }
        },
        leadingContent = {
            ExerciseImage(data = data)
        },
        trailingContent = {
            when (type) {
                ExerciseListItemType.EXERCISE -> {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View exercise details"
                    )
                }
                ExerciseListItemType.WORKOUT_NOT_ADDED_TO_EXERCISE -> {
                    IconButton(onClick = onAddToWorkout) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add exercise to workout"
                        )
                    }
                }
                ExerciseListItemType.WORKOUT_ADDED_TO_EXERCISE -> {
                    if (data is ExerciseListItemData.WorkoutExerciseData) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onModify(data.workoutExercise) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Modify exercise in workout"
                                )
                            }
                            IconButton(onClick = { onDelete(data.workoutExercise) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete exercise from workout"
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = Modifier
            .clickable {
                if (data is ExerciseListItemData.ExerciseData) {
                    onClick(data.exercise)
                }
            }
            .fillMaxWidth()
    )
}

@Composable
private fun ExerciseImage(data: ExerciseListItemData) {
    val imagePath = when (data) {
        is ExerciseListItemData.ExerciseData -> data.exercise.screenshotPath
        is ExerciseListItemData.WorkoutExerciseData -> data.workoutExercise.exercise?.screenshotPath
    }

    if (imagePath != null) {
        AsyncImage(
            model = imagePath,
            contentDescription = "Exercise image",
            modifier = Modifier.size(56.dp),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.error), // Placeholder during loading
            error = painterResource(id = R.drawable.error) // Error image
        )
    } else {
        Icon(
            painter = painterResource(id = R.drawable.error),
            contentDescription = "Exercise image not available",
            modifier = Modifier.size(56.dp)
        )
    }
}

@Composable
private fun WorkoutDetails(workoutExercise: WorkoutExercise) {
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Order: ${workoutExercise.order}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${workoutExercise.sets} sets",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${workoutExercise.reps} reps",
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Rest: ${workoutExercise.restBetweenSets}s",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}




// Preview for EXERCISE type
@Preview(showBackground = true, name = "Exercise Item")
@Composable
fun ExerciseListItemExercisePreview() {
    AppTheme {
        ExerciseListItem(
            data = ExerciseListItemData.ExerciseData(sampleExercise),
            type = ExerciseListItemType.EXERCISE,
            onClick = { /* No-op for preview */ },
            onAddToWorkout = { /* No-op for preview */ },
            onModify = { /* No-op for preview */ },
            onDelete = { /* No-op for preview */ }
        )
    }
}

// Preview for WORKOUT_ADDED_TO_EXERCISE type
@Preview(showBackground = true, name = "Workout Exercise Item (Added)")
@Composable
fun ExerciseListItemWorkoutAddedPreview() {
    AppTheme {
        ExerciseListItem(
            data = ExerciseListItemData.WorkoutExerciseData(sampleWorkoutExercise),
            type = ExerciseListItemType.WORKOUT_ADDED_TO_EXERCISE,
            onClick = { /* No-op for preview */ },
            onAddToWorkout = { /* No-op for preview */ },
            onModify = { /* No-op for preview */ },
            onDelete = { /* No-op for preview */ }
        )
    }
}

// Preview for WORKOUT_NOT_ADDED_TO_EXERCISE type
@Preview(showBackground = true, name = "Workout Exercise Item (Not Added)")
@Composable
fun ExerciseListItemWorkoutNotAddedPreview() {
    AppTheme {
        ExerciseListItem(
            data = ExerciseListItemData.ExerciseData(sampleExercise),
            type = ExerciseListItemType.WORKOUT_NOT_ADDED_TO_EXERCISE,
            onClick = { /* No-op for preview */ },
            onAddToWorkout = { /* No-op for preview */ },
            onModify = { /* No-op for preview */ },
            onDelete = { /* No-op for preview */ }
        )
    }
}