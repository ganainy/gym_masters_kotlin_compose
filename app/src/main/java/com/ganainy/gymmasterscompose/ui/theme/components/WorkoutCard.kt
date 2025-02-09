package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.WorkoutWithStatus

@Composable
fun WorkoutCard(
    workoutWithStatus: WorkoutWithStatus,
    onWorkoutLike: (Workout) -> Unit = {},
    onWorkoutSave: (Workout) -> Unit = {},
    onWorkoutClick: (Workout, Boolean, Boolean) -> Unit = { _, _, _ -> }
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                onWorkoutClick(
                    workoutWithStatus.workout,
                    workoutWithStatus.isLiked,
                    workoutWithStatus.isSaved
                )
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            // Workout Image
            Image(
                painter = rememberImagePainter(
                    data = workoutWithStatus.workout.imageUrl.ifEmpty { R.drawable.dumbbells }
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Stats Row
            WorkoutMetricsRow(
                workoutWithStatus = workoutWithStatus,
                onLikeClick = onWorkoutLike,
                onSaveClick = onWorkoutSave,
            )

            // Workout Title
            Text(
                text = workoutWithStatus.workout.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Workout Description
            Text(
                text = workoutWithStatus.workout.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Workout Details
            Text(
                text = "Duration: ${workoutWithStatus.workout.workoutDuration} mins",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Difficulty: ${workoutWithStatus.workout.difficulty}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Exercise List
            Text(
                text = "Exercises:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            workoutWithStatus.workout.workoutExerciseList.forEach { workoutExercise ->
                Text(
                    text = "- ${workoutExercise.exercise?.name}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview
@Composable
fun WorkoutCardPreview() {
    WorkoutCard(
        WorkoutWithStatus(
            workout =
            Workout(
                id = "1",
                title = "Workout Title",
                description = "Workout description",
                imageUrl = "",
                workoutDuration = "30",
                difficulty = "Intermediate",
                workoutExerciseList = listOf(
                    WorkoutExercise(
                        Exercise(
                            bodyPart = "chest",
                            equipment = "barbell",
                            gifUrl = "https://example.com/exercise1.gif",
                            name = "Bench Press",
                            target = "pectoralis major",
                            secondaryMuscles = listOf("anterior deltoid", "triceps brachii"),
                            instructions = listOf(
                                "Lie on a flat bench and grip the barbell with your hands slightly" +
                                        " wider than shoulder-width apart. Lower the barbell to your chest, then press upwards extending your arms fully.",
                            ),
                            id = "1",
                            screenshotPath = "123.jpg"
                        ), 1
                    ),
                    WorkoutExercise(
                        Exercise(
                            bodyPart = "chest",
                            equipment = "barbell",
                            gifUrl = "https://example.com/exercise1.gif",
                            name = "Bench Press",
                            target = "pectoralis major",
                            secondaryMuscles = listOf("anterior deltoid", "triceps brachii"),
                            instructions = listOf(
                                "Lie on a flat bench and grip the barbell with your hands slightly" +
                                        " wider than shoulder-width apart. Lower the barbell to your chest, then press upwards extending your arms fully.",
                            ),
                            id = "1",
                            screenshotPath = "123.jpg"
                        ), 1
                    ),

                    )
            ),
            isLiked = true,
            isSaved = false,
        ),
        {},
        {},
        {_ , _, _ -> })
}