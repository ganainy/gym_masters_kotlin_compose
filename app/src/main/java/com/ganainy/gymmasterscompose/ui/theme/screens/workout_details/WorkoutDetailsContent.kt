package com.ganainy.gymmasterscompose.ui.theme.screens.workout_details


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ganainy.gymmasterscompose.ui.theme.components.ExerciseListItem
import com.ganainy.gymmasterscompose.ui.theme.components.ExerciseListItemType
import com.ganainy.gymmasterscompose.ui.theme.components.WorkoutMetricsRow
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.WorkoutWithStatus

@Composable
fun WorkoutDetailsContent(
    workoutWithStatus: WorkoutWithStatus?,
    onWorkoutLike: (Workout) -> Unit = {},
    onWorkoutSave: (Workout) -> Unit = {},
    navigateToExerciseDetails: (Exercise) -> Unit = {},
) {
    if (workoutWithStatus == null) {
        return
    }

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
            ExerciseListItem(
                exercise = workoutExercise.exercise,
                type = ExerciseListItemType.EXERCISE,
                workoutExercise = workoutExercise,
                onClick = navigateToExerciseDetails,
            )
        }
    }
}

@Preview
@Composable
fun WorkoutDetailsContentPreview() {
    WorkoutDetailsContent(
        workoutWithStatus = WorkoutWithStatus(
            workout = Workout(
                id = "1",
                title = "Workout Title",
                description = "Workout Description",
                imageUrl = "",
                workoutDuration = "30",
                difficulty = "Beginner",
                workoutExerciseList = listOf(
                    WorkoutExercise(
                        exercise = Exercise(
                            id = "1",
                            name = "Exercise 1",
                            bodyPart = "Chest",
                            equipment = "Dumbbells",
                            target = "Chest",
                        ),
                        sets = 3,
                        reps = 8,
                    ),
                    WorkoutExercise(
                        exercise = Exercise(
                            id = "2",
                            name = "Exercise 2",
                            bodyPart = "Back",
                            equipment = "Pull up bar",
                            target = "Back",
                        ),
                        sets = 3,
                        reps = 12,
                    )
                )
            ),
            isLiked = false,
            isSaved = false
        )
    )
}