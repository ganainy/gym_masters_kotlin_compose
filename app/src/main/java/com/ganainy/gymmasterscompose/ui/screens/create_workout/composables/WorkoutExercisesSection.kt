package com.ganainy.gymmasterscompose.ui.screens.create_workout.composables

import ExerciseInWorkoutCreateItem
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutExercise

@Composable
internal fun WorkoutExercisesSection(
    exerciseList: List<WorkoutExercise>,
    onDeleteExercise: (WorkoutExercise) -> Unit,
    toggleExerciseWorkoutListShow: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section title + Add exercise icon
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Exercises",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add exercise",
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        onClick = toggleExerciseWorkoutListShow
                    )
                    .padding(4.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Empty state
        if (exerciseList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No exercises added yet",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        } else {
            // Exercise list
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                exerciseList.forEach { exercise ->
                    ExerciseInWorkoutCreateItem(
                        workoutExercise = exercise,
                        onDeleteExercise = onDeleteExercise
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutExercisesSectionPreview() {
    MaterialTheme {
        WorkoutExercisesSection(
            exerciseList = listOf(
                WorkoutExercise(
                    exercise = Exercise(name = "Bench Press"),
                    sets = 3,
                    reps = 10,
                    order = 1
                ),
                WorkoutExercise(
                    exercise = Exercise(name = "Squats"),
                    sets = 4,
                    reps = 12,
                    order = 2
                )
            ),
            onDeleteExercise = { /* Handle delete */ },
            toggleExerciseWorkoutListShow = { /* Handle toggle */ }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutExercisesSectionEmptyPreview() {
    MaterialTheme {
        WorkoutExercisesSection(
            exerciseList = emptyList(),
            onDeleteExercise = { /* Handle delete */ },
            toggleExerciseWorkoutListShow = { /* Handle toggle */ }
        )
    }
}
