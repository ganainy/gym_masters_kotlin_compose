package com.ganainy.gymmasterscompose.ui.screens.profile.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.User
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.screens.profile.ProfileScreenAction
import com.ganainy.gymmasterscompose.ui.screens.profile.ProfileUiData
import com.ganainy.gymmasterscompose.ui.screens.workout_list.WorkoutWithStatus
import com.ganainy.gymmasterscompose.ui.shared_components.ExpandableSection
import com.ganainy.gymmasterscompose.ui.shared_components.ProfileHeader

@Composable
internal fun OtherUserProfileContent(
    uiData: ProfileUiData,
    onAction: (ProfileScreenAction) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = {

        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            item {
                ProfileHeader(
                    user = uiData.user,
                    isOwnProfile = false,
                    stats = uiData.user.stats,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            item {
                FollowButton(
                    isFollowedByLoggedUser = uiData.isFollowing,
                    onFollowClick = { onAction(ProfileScreenAction.ToggleFollow(uiData.user.id)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            item {
                ExpandableSection(
                    header = "Posts",
                    content = {
                        ProfilePostsListComponent(
                            posts = uiData.posts,
                            onPostClick = { /* TODO: Handle post click */ },
                            onCreatePost = { onAction(ProfileScreenAction.NavigateToCreatePost) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            isOwnProfile = false
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun FollowButton(
    isFollowedByLoggedUser: Boolean,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onFollowClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowedByLoggedUser) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
            contentColor = if (isFollowedByLoggedUser) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = if (isFollowedByLoggedUser) "Unfollow" else "Follow",
            style = MaterialTheme.typography.labelLarge
        )
    }
}




@Preview(showBackground = true)
@Composable
fun OtherUserProfileContentPreview() {
    val user = User(
        id = "1",
        displayName = "John Doe",
        profilePictureUrl = "",
        bio = "Fitness enthusiast"
    )
    val posts = emptyList<FeedPost>()
    val isFollowing = false
    val workouts = emptyList<WorkoutWithStatus>()
    val isOwnProfile = false
    val exerciseList = emptyList<Exercise>()
AppTheme {
    OtherUserProfileContent(
        uiData = ProfileUiData(
            user = user,
            posts = posts,
            isFollowing = isFollowing,
            workoutWithStatusList = workouts,
            isOwnProfile = isOwnProfile,
            exerciseList = exerciseList
        ),
        onAction = {}
    )
}
}