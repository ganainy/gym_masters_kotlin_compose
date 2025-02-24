package com.ganainy.gymmasterscompose.ui.shared_components

import CustomChip
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutExercise
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
    // Animation for press effect
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 200)
    )

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .clip(RoundedCornerShape(12.dp))
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                enabled = data is ExerciseListItemData.ExerciseData,
                onClick = {
                    if (data is ExerciseListItemData.ExerciseData) {
                        onClick(data.exercise)
                    }
                }
            ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            headlineColor = MaterialTheme.colorScheme.onSurface,
            supportingColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        ),
        headlineContent = {
            Text(
                text = when (data) {
                    is ExerciseListItemData.ExerciseData -> data.exercise.name
                    is ExerciseListItemData.WorkoutExerciseData -> data.workoutExercise.exercise?.name.orEmpty()
                },
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            )
        },
        supportingContent = {
            Column {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    maxLines = 2,
                    overflow = FlowRowOverflow.Clip
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

                    if (bodyPart.isNotEmpty()) CustomChip(bodyPart, modifier = Modifier.padding(vertical = 2.dp))
                    if (equipment.isNotEmpty()) CustomChip(equipment, modifier = Modifier.padding(vertical = 2.dp))
                    if (target.isNotEmpty()) CustomChip(target, modifier = Modifier.padding(vertical = 2.dp))
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
                        contentDescription = "View exercise details",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                ExerciseListItemType.WORKOUT_NOT_ADDED_TO_EXERCISE -> {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add exercise to workout",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                onClick = onAddToWorkout
                            )
                            .padding(4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                ExerciseListItemType.WORKOUT_ADDED_TO_EXERCISE -> {
                    if (data is ExerciseListItemData.WorkoutExerciseData) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modify exercise in workout",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(
                                        onClick = { onModify(data.workoutExercise) }
                                    )
                                    .padding(4.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete exercise from workout",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(
                                        onClick = { onDelete(data.workoutExercise) }
                                    )
                                    .padding(4.dp),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
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
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.error),
            error = painterResource(id = R.drawable.error)
        )
    } else {
        Icon(
            painter = painterResource(id = R.drawable.error),
            contentDescription = "Exercise image not available",
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp)),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun WorkoutDetails(workoutExercise: WorkoutExercise) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

            Text(
                text = "Order: ${workoutExercise.order}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )
            Text(
                text = "${workoutExercise.sets} sets, ${workoutExercise.reps} reps",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )
            Text(
                text = "Rest: ${workoutExercise.restBetweenSets}s",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
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