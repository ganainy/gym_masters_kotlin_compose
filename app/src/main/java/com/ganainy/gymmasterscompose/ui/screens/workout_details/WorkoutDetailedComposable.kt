package com.ganainy.gymmasterscompose.ui.screens.workout_details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.ui.screens.create_workout.composables.WorkoutExercisesSection
import com.ganainy.gymmasterscompose.ui.screens.workout_list.WorkoutWithStatus
import com.ganainy.gymmasterscompose.ui.shared_components.HashtagText
import com.ganainy.gymmasterscompose.utils.MockData.sampleWorkoutWithStatus


data class DetailedOnlyParams(
    val onExerciseClick: (Exercise) -> Unit = {},
)

// Detailed Workout Composable
@Composable
fun WorkoutDetailedComposable(
    workoutWithStatus: WorkoutWithStatus,
    modifier: Modifier = Modifier,
    detailedOnlyParams: DetailedOnlyParams,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp) // Increased padding for better spacing
            .verticalScroll(rememberScrollState()), // Added scrollability for long content
    ) {
        // Header Section
        Column {
            // Workout Image (larger for detailed view)
            if (workoutWithStatus.workout.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(workoutWithStatus.workout.imageUrl.ifEmpty { null })
                        .placeholder(R.drawable.dumbbells)
                        .crossfade(true)
                        .error(R.drawable.dumbbells)
                        .build(),
                    contentDescription = "Workout Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Increased height for detailed view
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerPlaceholder(
                            visible = workoutWithStatus.workout.imageUrl.isNotEmpty(),
                            shimmerTheme = LocalShimmerTheme.current,
                        ),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Title
            HashtagText(
                text = workoutWithStatus.workout.title,
                style = MaterialTheme.typography.headlineMedium.copy( // Larger text style
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                onHashtagClick = { /*todo*/ },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Description
            if (workoutWithStatus.workout.description.isNotEmpty()) {
                Text(
                    text = workoutWithStatus.workout.description,
                    style = MaterialTheme.typography.bodyLarge, // Larger text for better readability
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // Stats Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Workout Details",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ExpandedWorkoutStat(
                        icon = Icons.Rounded.Timer,
                        value = "${workoutWithStatus.workout.workoutDuration.takeIf { it.isNotEmpty() } ?: "N/A"} mins",
                        label = "Duration"
                    )
                    ExpandedWorkoutStat(
                        icon = Icons.Rounded.FitnessCenter,
                        value = workoutWithStatus.workout.difficulty.takeIf { it.isNotEmpty() }
                            ?: "N/A",
                        label = "Difficulty"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exercises Section
        Column {
            Text(
                text = "Exercises",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            workoutWithStatus.workout.workoutExerciseList.forEachIndexed { index, workoutExercise ->
                ExerciseInWorkoutListItem(
                    workoutExercise = workoutExercise,
                    onClick = detailedOnlyParams.onExerciseClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                if (index < workoutWithStatus.workout.workoutExerciseList.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}


// Modified WorkoutStat to include label
@Composable
private fun ExpandedWorkoutStat(
    icon: ImageVector,
    value: String,
    label: String = ""
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Preview
@Composable
fun WorkoutDetailedComposablePreview() {
    AppTheme {
        WorkoutDetailedComposable(
            workoutWithStatus = sampleWorkoutWithStatus,
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
            .padding(vertical = 4.dp), // Compact padding
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

