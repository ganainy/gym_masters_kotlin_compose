package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.animation.LocalShimmerTheme
import com.ganainy.gymmasterscompose.animation.shimmerPlaceholder
import com.ganainy.gymmasterscompose.ui.theme.AppTheme
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_details.ExerciseInWorkoutListItem
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.WorkoutWithStatus
import com.ganainy.gymmasterscompose.utils.AndroidImageProcessor

enum class WorkoutViewType {
    PREVIEW,
    DETAILED;
}


@Composable
fun WorkoutCard(
    workoutWithStatus: WorkoutWithStatus,
    onWorkoutLike: (Workout) -> Unit = {},
    onWorkoutSave: (Workout) -> Unit = {},
    onWorkoutClick: (Workout, Boolean, Boolean) -> Unit = { _, _, _ -> },
    workoutViewType: WorkoutViewType = WorkoutViewType.PREVIEW,
    onExerciseClick: (Exercise) -> Unit = {},
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp)
            .clickable {
                onWorkoutClick(
                    workoutWithStatus.workout,
                    workoutWithStatus.isLiked,
                    workoutWithStatus.isSaved
                )
            },
    ) {
        Text(
            text = workoutWithStatus.workout.title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )

        if (workoutWithStatus.workout.description.isNotEmpty()) {
            Text(
                text = workoutWithStatus.workout.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            WorkoutStat(
                icon = Icons.Rounded.Timer,
                value = "${workoutWithStatus.workout.workoutDuration.takeIf { it.isNotEmpty() } ?: "N/A"} mins"
            )

            Spacer(modifier = Modifier.width(16.dp))

            WorkoutStat(
                icon = Icons.Rounded.FitnessCenter,
                value = workoutWithStatus.workout.difficulty.takeIf { it.isNotEmpty() } ?: "N/A"
            )
        }


        // Workout Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(workoutWithStatus.workout.imageUrl.ifEmpty { null }) // Avoids request if empty
                .crossfade(true)
                .error(R.drawable.dumbbells)
                .placeholder(R.drawable.dumbbells)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerPlaceholder(
                    visible = workoutWithStatus.workout.imageUrl.isNotEmpty(), // Shimmer only when loading
                    shimmerTheme = LocalShimmerTheme.current,
                ),
            contentScale = ContentScale.Crop
        )


        // Exercises Section
        when (workoutViewType) {
            WorkoutViewType.PREVIEW -> {
                    workoutWithStatus.workout.workoutExerciseList.forEachIndexed { index, workoutExercise ->
                        ExerciseItem(
                            exercise = workoutExercise.exercise,
                            isLast = index == workoutWithStatus.workout.workoutExerciseList.lastIndex
                        )
                    }
            }
            WorkoutViewType.DETAILED -> {
                // DetailedExercise List
                workoutWithStatus.workout.workoutExerciseList.forEach { workoutExercise ->
                    ExerciseInWorkoutListItem(
                        workoutExercise = workoutExercise,
                        onClick = onExerciseClick,
                    )
                }
            }
        }

        // Interaction Row
        when (workoutViewType) {
            WorkoutViewType.PREVIEW -> {
                WorkoutMetricsRow(
                    workoutWithStatus = workoutWithStatus,
                    onLikeClick = onWorkoutLike,
                    onSaveClick = onWorkoutSave,
                )
            }
            WorkoutViewType.DETAILED -> {}
        }
    }

}


@Composable
private fun WorkoutStat(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutStatPreview() {
    AppTheme {
        WorkoutStat(
            icon = Icons.Rounded.Timer,
            value = "30 mins"
        )
    }
}

@Composable
private fun ExerciseItem(
    exercise: Exercise?,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    CircleShape
                )
        )

        Text(
            text = exercise?.name ?: "",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        if (!isLast) {
            Box(
                modifier = Modifier
                    .height(16.dp)
                    .width(1.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
            )
        }
    }
}

@Preview
@Composable
fun WorkoutCardPreview() {
    AppTheme {
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
            { _, _, _ -> })
    }
}