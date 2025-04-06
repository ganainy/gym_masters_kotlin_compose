package com.ganainy.gymmasterscompose.ui.screens.profile.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import com.ganainy.gymmasterscompose.ui.shared_components.CustomButton
import com.ganainy.gymmasterscompose.ui.shared_components.ExpandableSection
import com.ganainy.gymmasterscompose.ui.shared_components.ProfileHeader

@Composable
internal fun CurrentUserProfileContent(
    uiData: ProfileUiData,
    onAction: (ProfileScreenAction) -> Unit
) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                ,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            // Profile Header
            item {
                ProfileHeader(
                    user = uiData.user,
                    stats = uiData.user.stats,
                    isOwnProfile = true,
                    onEditProfilePicture = { onAction(ProfileScreenAction.NavigateToEditProfile(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            // Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomButton(
                        onClick = { onAction(ProfileScreenAction.NavigateToWorkoutSetup) },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Add,
                        text = "Create Workout"
                    )

                    CustomButton(
                        onClick = { /* TODO: Navigate to edit profile */ },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Edit,
                        text = "Edit Profile"
                    )
                }

                CustomButton(
                    onClick = { onAction(ProfileScreenAction.Logout) },
                    icon = Icons.AutoMirrored.Filled.Logout,
                    text = "Logout"
                )

            }

            // Posts Section
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
                                .heightIn(max = 300.dp),
                            isOwnProfile = uiData.isOwnProfile
                        )
                    }
                )
            }

            // Saved Workouts Section
            item {
                ExpandableSection(
                    header = "Saved Workouts",
                    content = {
                        ProfileWorkoutListComponent(
                            workouts = uiData.workoutWithStatusList,
                            onAddWorkoutClick = { onAction(ProfileScreenAction.NavigateToWorkoutSetup) },
                            onWorkoutClick = {
                                onAction(ProfileScreenAction.NavigateToWorkoutDetails(it))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            isOwnProfile = uiData.isOwnProfile
                        )
                    }
                )
            }

            // Saved Exercises Section
            item {
                ExpandableSection(
                    header = "Saved Exercises",
                    content = {
                        ProfileExerciseListComponent(
                            exerciseList = uiData.exerciseList,
                            onExploreExercisesClick = { onAction(ProfileScreenAction.NavigateToExercisesList) },
                            onExerciseClick = {
                                onAction(ProfileScreenAction.NavigateToExerciseDetails(it))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            isOwnProfile = uiData.isOwnProfile
                        )
                    }
                )
            }
        }
}



@Preview(showBackground = true)
@Composable
fun CurrentUserProfileContentPreview() {
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
        CurrentUserProfileContent(
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