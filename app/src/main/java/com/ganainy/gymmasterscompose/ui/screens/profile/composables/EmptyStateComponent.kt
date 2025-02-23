package com.ganainy.gymmasterscompose.ui.screens.profile.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.screens.profile.EmptyStateActionType

@Composable
internal fun EmptyStateComponent(
    isOwnProfile: Boolean,
    onAction: () -> Unit,
    actionType: EmptyStateActionType,
    modifier: Modifier = Modifier
) {
    var titleText = ""
    var bodyText = ""
    var buttonText = ""

    when (actionType) {
        EmptyStateActionType.CREATE_POST_ACTION -> {
            titleText = if (isOwnProfile) {
                "Share Your Fitness Journey"
            } else {
                "No Posts Yet"
            }
            bodyText = if (isOwnProfile) {
                "Start sharing your workouts, progress, and inspire others!"
            } else {
                "This user hasn't posted anything yet"
            }
            buttonText = "Create First Post"
        }

        EmptyStateActionType.CREATE_WORKOUT_ACTION -> {
            titleText = if (isOwnProfile) {
                "Create Your First Workout"
            } else {
                "No Workouts Yet"
            }
            bodyText = if (isOwnProfile) {
                "Start creating your first workout and share it with the community!"
            } else {
                "This user hasn't created any workouts yet"
            }
            buttonText = "Create First Workout"
        }

        EmptyStateActionType.EXPLORE_EXERCISE_LIST_ACTION -> {
            titleText = if (isOwnProfile) {
                "Explore More Exercises"
            } else {
                "No Exercises Saved Yet"
            }
            bodyText = if (isOwnProfile) {
                "Explore more exercises and add them to your workout routines!"
            } else {
                "This user hasn't saved any exercises yet"
            }
            buttonText = "Explore Exercises"
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.PostAdd,
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Text(
            text = titleText,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Text(
            text = bodyText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (isOwnProfile) {
            Button(
                onClick = onAction,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText)
            }
        }
    }
}


@Preview(showBackground = true, name = "Own profile, create post")
@Composable
internal fun EmptyStateComponentPreview_OwnProfile_CreatePost() {
    AppTheme {
        EmptyStateComponent(
            isOwnProfile = true,
            onAction = {},
            actionType = EmptyStateActionType.CREATE_POST_ACTION,
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true, name = "Own profile, create workout")
@Composable
internal fun EmptyStateComponentPreview_OwnProfile_CreateWorkout() {
    AppTheme {
        EmptyStateComponent(
            isOwnProfile = true,
            onAction = {},
            actionType = EmptyStateActionType.CREATE_WORKOUT_ACTION,
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true, name = "Own profile, explore exercises")
@Composable
internal fun EmptyStateComponentPreview_OwnProfile_ExploreExercises() {
    AppTheme {
        EmptyStateComponent(
            isOwnProfile = true,
            onAction = {},
            actionType = EmptyStateActionType.EXPLORE_EXERCISE_LIST_ACTION,
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true, name = "Other profile, create post")
@Composable
internal fun EmptyStateComponentPreview_OtherProfile_CreatePost() {
    AppTheme {
        EmptyStateComponent(
            isOwnProfile = false,
            onAction = {},
            actionType = EmptyStateActionType.CREATE_POST_ACTION,
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true, name = "Other profile, create workout")
@Composable
internal fun EmptyStateComponentPreview_OtherProfile_CreateWorkout() {
    AppTheme {
        EmptyStateComponent(
            isOwnProfile = false,
            onAction = {},
            actionType = EmptyStateActionType.CREATE_WORKOUT_ACTION,
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true, name = "Other profile, explore exercises")
@Composable
internal fun EmptyStateComponentPreview_OtherProfile_ExploreExercises() {
    AppTheme {
        EmptyStateComponent(
            isOwnProfile = false,
            onAction = {},
            actionType = EmptyStateActionType.EXPLORE_EXERCISE_LIST_ACTION,
            modifier = Modifier
        )
    }
}