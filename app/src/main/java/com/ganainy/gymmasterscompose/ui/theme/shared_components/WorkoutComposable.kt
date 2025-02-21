package com.ganainy.gymmasterscompose.ui.theme.shared_components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
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
import com.ganainy.gymmasterscompose.ui.theme.screens.create_workout.composables.WorkoutExercisesSection
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_details.ExerciseInWorkoutListItem
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.WorkoutWithStatus

enum class WorkoutViewType { PREVIEW, DETAILED; }

data class PreviewOnlyParams(
    val onWorkoutLike: (Workout) -> Unit = {},
    val onWorkoutSave: (Workout) -> Unit = {},
    val onWorkoutClick: (Workout, Boolean, Boolean) -> Unit = { _, _, _ -> }
)

data class DetailedOnlyParams(
    val onExerciseClick: (Exercise) -> Unit = {},
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutComposable(
    workoutWithStatus: WorkoutWithStatus,
    modifier: Modifier = Modifier,
    workoutViewType: WorkoutViewType = WorkoutViewType.PREVIEW,
    previewOnlyParams: PreviewOnlyParams? = null,
    detailedOnlyParams: DetailedOnlyParams? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), // Rounded corners
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    )
    {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp)
                .clickable {
                    previewOnlyParams?.onWorkoutClick?.let {
                        it(
                            workoutWithStatus.workout,
                            workoutWithStatus.isLiked,
                            workoutWithStatus.isSaved
                        )
                    }
                },
        ) {
            HashtagText(
                text = workoutWithStatus.workout.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                onHashtagClick = {/*todo*/},
            )

            if (workoutWithStatus.workout.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = workoutWithStatus.workout.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
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




            Spacer(modifier = Modifier.height(4.dp))
            // Exercises Section
            when (workoutViewType) {
                WorkoutViewType.PREVIEW -> {

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Exercises :",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            workoutWithStatus.workout.workoutExerciseList.forEach { workoutExercise ->
                                WorkoutExerciseMiniComposable(
                                    workoutExercise = workoutExercise,
                                )
                            }
                    }
                }

                WorkoutViewType.DETAILED -> {
                    // DetailedExercise List
                    workoutWithStatus.workout.workoutExerciseList.forEach { workoutExercise ->
                        detailedOnlyParams?.onExerciseClick?.let {
                            ExerciseInWorkoutListItem(
                                workoutExercise = workoutExercise,
                                onClick = it,
                            )
                        }
                    }
                }
            }

            // Workout Image
            if (workoutWithStatus.workout.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(workoutWithStatus.workout.imageUrl.ifEmpty { null }) // Avoids request if empty
                        .placeholder(R.drawable.dumbbells)
                        .crossfade(true)
                        .error(R.drawable.dumbbells)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .shimmerPlaceholder(
                            visible = workoutWithStatus.workout.imageUrl.isNotEmpty(), // Shimmer only when loading
                            shimmerTheme = LocalShimmerTheme.current,
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            // Interaction Row
            when (workoutViewType) {
                WorkoutViewType.PREVIEW -> {
                    if (previewOnlyParams != null) {
                        WorkoutMetricsRow(
                            workoutWithStatus = workoutWithStatus,
                            onLikeClick = previewOnlyParams.onWorkoutLike,
                            onSaveClick = previewOnlyParams.onWorkoutSave,
                        )
                    }
                }

                WorkoutViewType.DETAILED -> {}
            }
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


        @Preview
        @Composable
        fun WorkoutCardPreview() {
            AppTheme {
                WorkoutComposable(
                    WorkoutWithStatus(
                        workout =
                        Workout(
                            id = "1",
                            tags = listOf("tag"),
                            title = "WorkoutTitle #tag ",
                            description = "Workout description",
                            imageUrl = "randomUrl",
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
                                        secondaryMuscles = listOf(
                                            "anterior deltoid",
                                            "triceps brachii"
                                        ),
                                        instructions = listOf(
                                            "Lie on a flat bench and grip the barbell with your hands slightly" +
                                                    " wider than shoulder-width apart. Lower the barbell to your chest, then press upwards extending your arms fully.",
                                        ),
                                        id = "1",
                                        screenshotPath = "/storage/emulated/0/Download/exercise.jpg",

                                    ), order = 1
                                ),
                                WorkoutExercise(
                                    Exercise(
                                        bodyPart = "chest",
                                        equipment = "barbell",
                                        gifUrl = "https://example.com/exercise1.gif",
                                        name = "Bench Press",
                                        target = "pectoralis major",
                                        secondaryMuscles = listOf(
                                            "anterior deltoid",
                                            "triceps brachii"
                                        ),
                                        instructions = listOf(
                                            "Lie on a flat bench and grip the barbell with your hands slightly" +
                                                    " wider than shoulder-width apart. Lower the barbell to your chest, then press upwards extending your arms fully.",
                                        ),
                                        id = "2",
                                        screenshotPath = "123.jpg"
                                    ), order = 2
                                ),

                                )
                        ),
                        isLiked = true,
                        isSaved = false,
                    ),
                    workoutViewType = WorkoutViewType.PREVIEW,
                    previewOnlyParams = PreviewOnlyParams(),
                    detailedOnlyParams = DetailedOnlyParams(),

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


@Composable
internal fun WorkoutExerciseMiniComposable(
    workoutExercise: WorkoutExercise,
    modifier: Modifier = Modifier
) {


        Row(
            modifier = modifier
                .padding( vertical = 4.dp), // Compact padding
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Order indicator
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .wrapContentSize(Alignment.Center)
            ) {
                Text(
                    text = "${workoutExercise.order}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(12.dp)) // Space between order and content

            // Exercise name
                workoutExercise.exercise?.name?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutExercisesMiniComposablePreview() {
    MaterialTheme {
        WorkoutExerciseMiniComposable(
            workoutExercise =
                WorkoutExercise(
                    exercise = Exercise(name = "Squats"),
                    sets = 4,
                    reps = 12,
                    order = 1
            ),
        )
    }
}

