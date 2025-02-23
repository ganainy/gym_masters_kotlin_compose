package com.ganainy.gymmasterscompose.ui.screens.workout_details


import CustomChip
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutExercise


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseInWorkoutListItem(
    workoutExercise: WorkoutExercise,
    onClick: (Exercise) -> Unit,
    modifier: Modifier = Modifier,
) {


    ListItem(
        headlineContent = {
            workoutExercise.exercise?.name?.let { Text(it) }
        },
        supportingContent = {
            Column {
                FlowRow(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    workoutExercise.exercise?.bodyPart?.let { CustomChip(it) }

                    workoutExercise.exercise?.equipment?.let { CustomChip(it) }

                    workoutExercise.exercise?.target?.let { CustomChip(it) }
                }

                Spacer(modifier = Modifier.height(4.dp))
            //todo
            }
        },
        leadingContent = {
            // If an imageBitmap is provided, use it. Otherwise, show placeholder
            if (workoutExercise.exercise?.screenshotPath != null) {
                Image(
                    painter = rememberImagePainter(data = workoutExercise.exercise.screenshotPath),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.dumbbell), // Placeholder icon
                    contentDescription = "Placeholder",
                    modifier = Modifier.size(56.dp)
                )
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

@Composable
fun ExerciseDetailsCard(
    workoutExercise: WorkoutExercise,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // First row: Order, Sets, and Reps
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExerciseDetailItem(
                    icon = Icons.Rounded.FormatListNumbered,
                    value = workoutExercise.order.toString(),
                    label = "Order",
                    color = MaterialTheme.colorScheme.primary
                )

                ExerciseDetailItem(
                    icon = Icons.Rounded.Repeat,
                    value = workoutExercise.sets.toString(),
                    label = "Sets",
                    color = MaterialTheme.colorScheme.secondary
                )

                ExerciseDetailItem(
                    icon = Icons.Rounded.FitnessCenter,
                    value = workoutExercise.reps.toString(),
                    label = "Reps",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // Second row: Rest time
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExerciseDetailItem(
                    icon = Icons.Rounded.Timer,
                    value = "${workoutExercise.restBetweenSets}s",
                    label = "Rest",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ExerciseDetailItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$label icon",
            tint = color,
            modifier = Modifier
                .size(20.dp)
                .padding(end = 4.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExerciseDetailsCardPreview() {
    // Create a sample WorkoutExercise for preview
    val sampleExercise = WorkoutExercise(
        order = 1,
        sets = 3,
        reps = 12,
        restBetweenSets = 60,
    )

    AppTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            ExerciseDetailsCard(
                workoutExercise = sampleExercise
            )
        }
    }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    )
}


