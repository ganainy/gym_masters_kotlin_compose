package com.ganainy.gymmasterscompose.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.screens.profile.composables.CurrentUserProfileContent
import com.ganainy.gymmasterscompose.ui.screens.profile.composables.OtherUserProfileContent
import com.ganainy.gymmasterscompose.ui.screens.workout_list.WorkoutWithStatus
import com.ganainy.gymmasterscompose.ui.shared_components.CustomTopAppBar
import com.ganainy.gymmasterscompose.ui.shared_components.LoadingIndicator
import com.ganainy.gymmasterscompose.utils.MockData.sampleExerciseListSmall
import com.ganainy.gymmasterscompose.utils.MockData.samplePostList
import com.ganainy.gymmasterscompose.utils.MockData.sampleUser
import com.ganainy.gymmasterscompose.utils.MockData.sampleWorkoutWithStatus
import com.ganainy.gymmasterscompose.utils.Utils.showToast

//todo fix infinite loading on opening other user profile
@Composable
fun ProfileScreen(
    userId: String?,
    navigateBack: () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToCreatePost: () -> Unit,
    navigateToWorkoutSetup: () -> Unit,
    navigateToWorkoutDetails: (WorkoutWithStatus) -> Unit,
    navigateToExercisesList: () -> Unit,
    navigateToExerciseDetails: (Exercise) -> Unit
) {
    val viewModel = hiltViewModel<ProfileViewModel>()

    viewModel.loadProfile(userId)

    val uiData by viewModel.uiData.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val onAction: (ProfileScreenAction) -> Unit = { action ->
        when (action) {
            ProfileScreenAction.NavigateToCreatePost -> navigateToCreatePost()
            ProfileScreenAction.NavigateBack -> navigateBack()
            is ProfileScreenAction.NavigateToEditProfile -> viewModel.onEditProfilePicture(action.imagePath)
            is ProfileScreenAction.NavigateToExerciseDetails -> navigateToExerciseDetails(action.exercise)
            ProfileScreenAction.NavigateToExercisesList -> navigateToExercisesList()
            ProfileScreenAction.NavigateToLogin -> navigateToLogin()
            is ProfileScreenAction.NavigateToWorkoutDetails -> navigateToWorkoutDetails(action.workoutWithStatus)
            ProfileScreenAction.NavigateToWorkoutSetup -> navigateToWorkoutSetup()
            is ProfileScreenAction.Logout -> viewModel.logout {
                navigateToLogin()
            }

            is ProfileScreenAction.ToggleFollow -> viewModel.toggleFollow(action.userToFollowOrUnfollowId)
        }
    }

    ProfileScreenContent(uiState, uiData, onAction)


}

@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState,
    uiData: ProfileUiData,
    onAction: (ProfileScreenAction) -> Unit
) {
    Column() {
        CustomTopAppBar(
            title = "Profile",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = { onAction(ProfileScreenAction.NavigateBack) },
        )

        Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                // Loading state
                LoadingIndicator()
            }

            is ProfileUiState.Error -> {
                // Error state
                when (state) {
                    is ProfileUiState.Error.IntError -> {
                        val errorMessage = stringResource(state.messageStringResource)
                        showToast(context = LocalContext.current, message = errorMessage)
                    }

                    is ProfileUiState.Error.StringError -> {
                        showToast(context = LocalContext.current, message = state.message)
                    }
                }
            }

            is ProfileUiState.Success -> {
                val profileType = state.profileType
                when (profileType) {
                    ProfileType.CURRENT_USER -> {
                        // Local user profile
                        CurrentUserProfileContent(
                            uiData = uiData,
                            onAction = onAction,
                        )
                    }

                    ProfileType.OTHER_USER -> {
                        // Other user profile
                        OtherUserProfileContent(
                            uiData = uiData,
                            onAction = onAction,
                        )
                    }
                }

            }
        }

    }
}


enum class EmptyStateActionType {
    CREATE_POST_ACTION,
    CREATE_WORKOUT_ACTION,
    EXPLORE_EXERCISE_LIST_ACTION
}

@Preview(showBackground = true)
@Composable
private fun PreviewProfileScreenContent() {
    AppTheme {
        ProfileScreenContent(
            uiState = ProfileUiState.Success(ProfileType.CURRENT_USER),
            uiData = ProfileUiData(
                user = sampleUser,
                posts = samplePostList,
                workoutWithStatusList = listOf(sampleWorkoutWithStatus),
                exerciseList = sampleExerciseListSmall,
                isFollowing = false
            ),
            onAction = {}
        )
    }
}