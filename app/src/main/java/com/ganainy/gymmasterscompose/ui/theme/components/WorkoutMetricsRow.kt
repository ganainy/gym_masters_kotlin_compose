package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.R
import androidx.compose.foundation.layout.size
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutMetrics
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.WorkoutWithStatus

@Composable
fun WorkoutMetricsRow(
    workoutWithStatus: WorkoutWithStatus,
    onLikeClick: (Workout) -> Unit = {},
    onSaveClick: (Workout) -> Unit = {},
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = if (workoutWithStatus.isLiked) R.drawable.liked_filled else R.drawable.like_outlined),
                contentDescription = "Like Icon",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onLikeClick(workoutWithStatus.workout) }

            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = workoutWithStatus.workout.workoutMetrics.likesCount.toString(), fontSize = 14.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = if (workoutWithStatus.isSaved) R.drawable.save_filled else R.drawable.save_outlined),
                contentDescription = "Save Icon",
                modifier = Modifier.size(24.dp).clickable { onSaveClick(workoutWithStatus.workout) }

            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = workoutWithStatus.workout.workoutMetrics.saveCount.toString(), fontSize = 14.sp)
        }


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