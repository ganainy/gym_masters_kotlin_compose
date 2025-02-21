package com.ganainy.gymmasterscompose.ui.theme.shared_components

import AnimatedLikeCounter
import AnimatedSaveCounter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutMetrics
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.WorkoutWithStatus


@Composable
fun WorkoutMetricsRow(
    workoutWithStatus: WorkoutWithStatus,
    onLikeClick: (Workout) -> Unit = {},
    onSaveClick: (Workout) -> Unit = {},
) {

    Column {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            //Like icon and counter
            AnimatedLikeCounter(isLiked = workoutWithStatus.isLiked,
                count = workoutWithStatus.workout.workoutMetrics.likesCount,
                onLikeClick = {onLikeClick(workoutWithStatus.workout)},
            )

            Box(
                modifier = Modifier
                    .size(1.dp, 24.dp)
                    .padding(horizontal = 8.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            ){
                VerticalDivider(
                    thickness = 1.dp
                )
            }

            //Save icon and counter
            AnimatedSaveCounter(
                count = workoutWithStatus.workout.workoutMetrics.saveCount,
                isSaved = workoutWithStatus.isSaved,
                onSaveClick = {onSaveClick(workoutWithStatus.workout)},
                showZeroCount =false
            )

        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewWorkoutMetricsRow() {
    WorkoutMetricsRow(
        workoutWithStatus = WorkoutWithStatus(
            workout = Workout(
                imageUrl = "",
                title = "",
                workoutMetrics = WorkoutMetrics(likesCount = 0, saveCount = 0)
            ),
            isLiked = false,
            isSaved = false
        )
    ) {}
}