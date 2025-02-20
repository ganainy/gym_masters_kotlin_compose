package com.ganainy.gymmasterscompose.ui.theme.screens.workout_details


import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.ui.theme.components.CustomChip
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.utils.Utils.getBitmapFromPath


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseInWorkoutListItem(
    workoutExercise: WorkoutExercise,
    onClick: (Exercise) -> Unit,
) {


    ListItem(
        headlineContent = {
            workoutExercise.exercise?.name?.let { Text(it) }
        },
        supportingContent = {
            Column {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    workoutExercise.exercise?.bodyPart?.let { CustomChip(it) }

                    workoutExercise.exercise?.equipment?.let { CustomChip(it) }

                    workoutExercise.exercise?.target?.let { CustomChip(it) }
                }

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
        },
        leadingContent = {
            workoutExercise.exercise?.screenshotPath?.let { screenshotPath ->
                getBitmapFromPath(screenshotPath)?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, "View details")

        },
        modifier = Modifier
            .clickable {
                if (workoutExercise.exercise != null) {
                    onClick(workoutExercise.exercise)
                }
            }
            .fillMaxWidth()
    )
}


@Preview
@Composable
private fun ExerciseInWorkoutListItemPreview() {
    ExerciseInWorkoutListItem(
        workoutExercise = WorkoutExercise(
            exercise = Exercise(
                bodyPart = "Legs",
                equipment = "Machine",
                gifUrl = "url_to_squat_machine_gif",
                screenshotPath = "path_to_squat_machine_screenshot",
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
            order = 1,
            sets = 3,
            reps = 8,
            restBetweenSets = 60
        ),
        onClick = {},
    )
}


