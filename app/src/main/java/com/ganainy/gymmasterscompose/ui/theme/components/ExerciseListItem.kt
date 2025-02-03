package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.utils.Utils.getBitmapFromPath

enum class ExerciseListItemType {
    EXERCISE,
    WORKOUT_ADDED_TO_EXERCISE,
    WORKOUT_NOT_ADDED_TO_EXERCISE
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseListItem(
    exercise: Exercise?=null,
    type: ExerciseListItemType,
    workoutExercise: WorkoutExercise? = null,
    onClick: ((Exercise) -> Unit)? = null,
    onAddToWorkout: (() -> Unit)? = null,
    onModify: ((WorkoutExercise) -> Unit)? = null,
    onDelete: ((WorkoutExercise) -> Unit)? = null
) {
    val backgroundColor = when (type) {
        ExerciseListItemType.WORKOUT_ADDED_TO_EXERCISE -> Color(0xFFC8E6C9) // Light green background
        else -> MaterialTheme.colorScheme.surface
    }

    ListItem(
        headlineContent = {
            if (exercise != null) {
                Text(exercise.name)
            }else if (workoutExercise != null) {
                workoutExercise.exercise?.name?.let { Text(it) }
            }
        },
        supportingContent = {
            Column {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (exercise != null) {
                        CustomChip(exercise.bodyPart)
                    }else if (workoutExercise != null) {
                        workoutExercise.exercise?.bodyPart?.let { CustomChip(it) }
                    }
                    if (exercise != null) {
                        CustomChip(exercise.equipment)
                    }else if (workoutExercise != null) {
                        workoutExercise.exercise?.equipment?.let { CustomChip(it) }
                    }
                    if (exercise != null) {
                        CustomChip(exercise.target)
                    }else if (workoutExercise != null) {
                        workoutExercise.exercise?.target?.let { CustomChip(it) }
                    }
                }

                if (type == ExerciseListItemType.WORKOUT_ADDED_TO_EXERCISE && workoutExercise != null) {
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
            }
        },
        leadingContent = {
            if (exercise != null) {
                exercise.screenshotPath?.let { screenshotPath ->
                    getBitmapFromPath(screenshotPath)?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }else if (workoutExercise != null) {
                workoutExercise.exercise?.screenshotPath?.let { screenshotPath ->
                    getBitmapFromPath(screenshotPath)?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }
        },
        trailingContent = {
            when (type) {
                ExerciseListItemType.EXERCISE -> {
                    Icon(Icons.Default.ChevronRight, "View details")
                }
                ExerciseListItemType.WORKOUT_NOT_ADDED_TO_EXERCISE -> {
                    IconButton(onClick = { onAddToWorkout?.invoke() }) {
                        Icon(Icons.Default.Add, "Add to workout")
                    }
                }
                ExerciseListItemType.WORKOUT_ADDED_TO_EXERCISE -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (workoutExercise != null) {
                                onModify?.invoke(workoutExercise)
                            }
                        }) {
                            Icon(Icons.Default.Edit, "Modify exercise")
                        }
                        IconButton(onClick = {
                            if (workoutExercise != null) {
                                onDelete?.invoke(workoutExercise)
                            }
                        }) {
                            Icon(Icons.Default.Delete, "Delete exercise")
                        }
                    }
                }
            }
        },
        modifier = Modifier
            .clickable {
                if (onClick != null) {
                    if (exercise != null) {
                        onClick(exercise)
                    }
                }
            }
            .background(backgroundColor)
            .fillMaxWidth()
    )
}



@Preview
@Composable
private fun ExerciseListItemPreview() {
    ExerciseListItem(
        exercise = Exercise(
            bodyPart = "Legs",
            equipment = "Machine",
            gifUrl = "url_to_squat_machine_gif",
            screenshotPath ="path_to_squat_machine_screenshot",
            id = "1",
            name = "Squat (Machine)",
            target = "Quadriceps",
            secondaryMuscles = listOf("Glutes", "Hamstrings"),
            instructions = listOf(
                "Set the machine to your height.",
                "Place your shoulders under the pads.",
                "Push through your heels to lift."
            )
        ),
        type = ExerciseListItemType.EXERCISE,
        onClick = {},
        onAddToWorkout = null,
        onModify = null,
        onDelete = null
    )
}


@Preview
@Composable
private fun ExerciseListItemPreview2() {
    ExerciseListItem(
        exercise = Exercise(
            bodyPart = "Legs",
            equipment = "Machine",
            gifUrl = "url_to_squat_machine_gif",
            screenshotPath ="path_to_squat_machine_screenshot",
            id = "1",
            name = "Squat (Machine)",
            target = "Quadriceps",
            secondaryMuscles = listOf("Glutes", "Hamstrings"),
            instructions = listOf(
                "Set the machine to your height.",
                "Place your shoulders under the pads.",
                "Push through your heels to lift."
            )
        ),
        type = ExerciseListItemType.WORKOUT_ADDED_TO_EXERCISE,
        onClick = {},
        onAddToWorkout = null,
        onModify = null,
        onDelete = null
    )
}


@Preview
@Composable
private fun ExerciseListItemPreview3() {
    ExerciseListItem(
        exercise = Exercise(
            bodyPart = "Legs",
            equipment = "Machine",
            gifUrl = "url_to_squat_machine_gif",
            screenshotPath ="path_to_squat_machine_screenshot",
            id = "1",
            name = "Squat (Machine)",
            target = "Quadriceps",
            secondaryMuscles = listOf("Glutes", "Hamstrings"),
            instructions = listOf(
                "Set the machine to your height.",
                "Place your shoulders under the pads.",
                "Push through your heels to lift."
            )
        ),
        type = ExerciseListItemType.WORKOUT_NOT_ADDED_TO_EXERCISE,
        onClick = {},
        onAddToWorkout = null,
        onModify = null,
        onDelete = null
    )
}