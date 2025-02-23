package com.ganainy.gymmasterscompose.ui.screens.profile.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.screens.profile.EmptyStateActionType
import com.ganainy.gymmasterscompose.ui.shared_components.ExerciseListItemData
import com.ganainy.gymmasterscompose.utils.MockData

@Composable
internal fun ProfileExerciseListComponent(
    exerciseList: List<Exercise>,
    onExploreExercisesClick: () -> Unit,
    onExerciseClick: (Exercise) -> Unit,
    isOwnProfile: Boolean,
    modifier: Modifier = Modifier
) {

    if (exerciseList.isEmpty()) {
        EmptyStateComponent(
            isOwnProfile = isOwnProfile,
            onAction = onExploreExercisesClick,
            actionType = EmptyStateActionType.EXPLORE_EXERCISE_LIST_ACTION,
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = exerciseList,
                key = { it.id }
            ) { exercise ->
                ProfileExerciseComponent(
                    exercise = ExerciseListItemData.ExerciseData(exercise).exercise,
                    onExerciseClick = { onExerciseClick(exercise) },
                )
                if (exercise != exerciseList.last()) {
                    HorizontalDivider()
            }
        }
    }
}
}


@Composable
fun ProfileExerciseComponent(
    exercise: Exercise,
    onExerciseClick: (Exercise) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onExerciseClick(exercise) }
           ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Exercise image - small size on left
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(exercise.gifUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Exercise Image",
            modifier = Modifier
                .size(64.dp) // Fixed square size for the image
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp)) // Space between image and text

        // Column for all text content
        Column(
            modifier = Modifier
                .weight(1f) // Take remaining space
                .padding(start = 8.dp)
        ) {
            // Exercise name
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Exercise metrics (body part and secondary muscles)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Body Part",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = exercise.bodyPart,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.target),
                        contentDescription = "Target Muscle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = exercise.target,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "ExerciseList",
    group = "Exercise List"
)
@Composable
fun ExerciseListPreview() {
    AppTheme {
        ProfileExerciseListComponent(
            exerciseList = MockData.sampleExerciseListSmall,
            onExploreExercisesClick = { /* No-op for preview */ },
            onExerciseClick = { /* No-op for preview */ },
            isOwnProfile = true
        )
    }
}