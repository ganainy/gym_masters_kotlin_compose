package com.ganainy.gymmasterscompose.ui.theme.components

import AnimatedLikeCounter
import AnimatedSaveCounter
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutMetrics
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.WorkoutWithStatus
import kotlinx.coroutines.launch

@Composable
fun WorkoutMetricsRow22(
    workoutWithStatus: WorkoutWithStatus,
    onLikeClick: (Workout) -> Unit = {},
    onSaveClick: (Workout) -> Unit = {},
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var lastLikeClickTime by remember { mutableStateOf(0L) }
    var lastSaveClickTime by remember { mutableStateOf(0L) }
    val CLICK_DEBOUNCE_TIME = 1000L

    val debouncedOnLikeClick = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLikeClickTime >= CLICK_DEBOUNCE_TIME) {
            lastLikeClickTime = currentTime
            onLikeClick(workoutWithStatus.workout)
        }else{
            scope.launch {
                Toast.makeText(context, "Please wait a moment before liking again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val debouncedOnSaveClick = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSaveClickTime >= CLICK_DEBOUNCE_TIME) {
            lastSaveClickTime = currentTime
            onSaveClick(workoutWithStatus.workout)
        }else{
            scope.launch {
                Toast.makeText(context, "Please wait a moment before saving again", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                    .clickable {
                        debouncedOnLikeClick()
                    }

            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = workoutWithStatus.workout.workoutMetrics.likesCount.toString(), fontSize = 14.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = if (workoutWithStatus.isSaved) R.drawable.save_filled else R.drawable.save_outlined),
                contentDescription = "Save Icon",
                modifier = Modifier.size(24.dp).clickable {
                    debouncedOnSaveClick()
                }

            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = workoutWithStatus.workout.workoutMetrics.saveCount.toString(), fontSize = 14.sp)
        }


    }

}


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