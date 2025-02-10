package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutMetrics
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.WorkoutWithStatus
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTime

@Preview
@Composable
fun PreviewWorkoutListItem() {
    WorkoutListItem(
    WorkoutWithStatus(
        workout = Workout(
            id = "id",
            creatorId = "creatorId",
            title = "title",
            description = "description",
            difficulty = "difficulty",
            workoutDuration = "50",
            dateCreated = 1643723400,
            imageUrl = "imageUrl",
            imagePath = "",
            tags = emptyList(),
            isPublic = false,
            workoutExerciseList = emptyList(),
            workoutMetrics = WorkoutMetrics()
        ),
        isLiked = false, isSaved = false,
    ), onWorkoutClick = {})
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutListItem(
    workoutWithStatus: WorkoutWithStatus,
    onWorkoutClick: (WorkoutWithStatus) -> Unit,
) {
    // Workout item container
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(16.dp)
            .clickable { onWorkoutClick(workoutWithStatus) }
    ) {
        // Workout timestamp
        if (workoutWithStatus.workout.dateCreated != 0L)
        Text(
            text = formatRelativeTime(workoutWithStatus.workout.dateCreated),
            fontSize = 12.sp,
            color = Color.Gray
        )

        // Spacer for vertical spacing
        Spacer(modifier = Modifier.height(8.dp))
        // Workout title
        Text(
            text = workoutWithStatus.workout.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        // Workout content
        Text(
            text = workoutWithStatus.workout.description,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Spacer for vertical spacing
        Spacer(modifier = Modifier.height(4.dp))

        // Workout Tags/Chips
        if (workoutWithStatus.workout.tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                content = {
                    workoutWithStatus.workout.tags.forEach { tag ->
                        // Individual hashtag chip
                        Text(
                            text = "#${tag}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .background(Color(0xFFF5F5F5))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }
            )
        }

        // Spacer for vertical spacing
        Spacer(modifier = Modifier.height(4.dp))

    }
}
