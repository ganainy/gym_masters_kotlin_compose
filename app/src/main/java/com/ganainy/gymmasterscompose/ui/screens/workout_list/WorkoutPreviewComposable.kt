package com.ganainy.gymmasterscompose.ui.screens.workout_list
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.ganainy.gymmasterscompose.ui.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.screens.workout_details.WorkoutExerciseMiniComposable
import com.ganainy.gymmasterscompose.ui.shared_components.HashtagText
import com.ganainy.gymmasterscompose.ui.shared_components.WorkoutMetricsRow
import com.ganainy.gymmasterscompose.ui.shared_components.WorkoutStat
import com.ganainy.gymmasterscompose.utils.MockData.sampleWorkoutWithStatus

data class PreviewOnlyParams(
    val onWorkoutLike: (Workout) -> Unit = {},
    val onWorkoutSave: (Workout) -> Unit = {},
    val onWorkoutClick: (Workout, Boolean, Boolean) -> Unit = { _, _, _ -> }
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutPreviewComposable(
    workoutWithStatus: WorkoutWithStatus,
    modifier: Modifier = Modifier,
    previewOnlyParams: PreviewOnlyParams,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp)
                .clickable {
                    previewOnlyParams.onWorkoutClick(
                        workoutWithStatus.workout,
                        workoutWithStatus.isLiked,
                        workoutWithStatus.isSaved
                    )
                },
        ) {
            HashtagText(
                text = workoutWithStatus.workout.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                onHashtagClick = { /*todo*/ },
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
                modifier = Modifier.fillMaxWidth(),
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

            // Exercises Preview
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

            // Workout Image
            if (workoutWithStatus.workout.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(workoutWithStatus.workout.imageUrl.ifEmpty { null })
                        .placeholder(R.drawable.dumbbells)
                        .crossfade(true)
                        .error(R.drawable.dumbbells)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .shimmerPlaceholder(
                            visible = workoutWithStatus.workout.imageUrl.isNotEmpty(),
                            shimmerTheme = LocalShimmerTheme.current,
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            // Interaction Row
            WorkoutMetricsRow(
                workoutWithStatus = workoutWithStatus,
                onLikeClick = previewOnlyParams.onWorkoutLike,
                onSaveClick = previewOnlyParams.onWorkoutSave,
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun WorkoutPreviewComposablePreview() {
    MaterialTheme {
        WorkoutPreviewComposable(
            workoutWithStatus = sampleWorkoutWithStatus,
            previewOnlyParams = PreviewOnlyParams(),
        )
    }
}
