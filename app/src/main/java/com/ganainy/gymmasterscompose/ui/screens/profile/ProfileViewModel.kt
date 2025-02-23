package com.ganainy.gymmasterscompose.ui.screens.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.User
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.repository.IAuthRepository
import com.ganainy.gymmasterscompose.ui.repository.IExerciseRepository
import com.ganainy.gymmasterscompose.ui.repository.ILikeRepository
import com.ganainy.gymmasterscompose.ui.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.repository.IWorkoutRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import com.ganainy.gymmasterscompose.ui.repository.onError
import com.ganainy.gymmasterscompose.ui.repository.onSuccess
import com.ganainy.gymmasterscompose.ui.room.CachedLike
import com.ganainy.gymmasterscompose.ui.room.LikeType
import com.ganainy.gymmasterscompose.ui.screens.workout_list.WorkoutWithStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ProfileUiData(
    val user: User = User(),
    val posts: List<FeedPost> = emptyList(),
    val isFollowing: Boolean = false,
    val workoutWithStatusList: List<WorkoutWithStatus> = emptyList(),
    val isOwnProfile: Boolean = false,
    val exerciseList: List<Exercise> = emptyList()
) {
}

sealed class ProfileUiState {
    data class Success(val profileType: ProfileType) : ProfileUiState()
    object Loading : ProfileUiState()
    sealed class Error : ProfileUiState() {
        data class IntError(val messageStringResource: Int) : Error()
        data class StringError(val message: String) : Error()
    }
}

enum class ProfileType {
    CURRENT_USER,
    OTHER_USER
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val application: Application,
    private val userRepository: IUserRepository,
    private val authRepository: IAuthRepository,
    private val socialRepository: ISocialRepository,
    private val workoutRepository: IWorkoutRepository,
    private val likeRepository: ILikeRepository,
    private val exerciseRepository: IExerciseRepository
) : ViewModel() {

    val context = application

    private val _uiData = MutableStateFlow(ProfileUiData())
    val uiData = _uiData.asStateFlow()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState = _uiState.asStateFlow()


    private val currentUserId: String
        get() = userRepository.getCurrentUserId()


    //if no userId is provided, the current user's profile is loaded otherwise the profile of the user with the provided id
    fun loadProfile(userId: String?) {
        viewModelScope.launch {
            val targetUserId = userId ?: currentUserId

            _uiData.update {
                it.copy(isOwnProfile = currentUserId == userId)
            }

            combine(
                userRepository.getUserFlow(targetUserId),
                userRepository.getUserPosts(targetUserId),
                socialRepository.isFollowing(targetUserId),
                workoutRepository.getLocalWorkoutsFlow(),
                likeRepository.observeTypeLikes(type = LikeType.WORKOUT, userId = targetUserId),
                exerciseRepository.observeSavedExercises()
            ) { results ->
                when {
                    results[0] is ResultWrapper.Error -> ResultWrapper.Error((results[0] as ResultWrapper.Error).exception)
                    results[1] is ResultWrapper.Error -> ResultWrapper.Error((results[1] as ResultWrapper.Error).exception)
                    results[2] is ResultWrapper.Error -> ResultWrapper.Error((results[2] as ResultWrapper.Error).exception)
                    results[3] is ResultWrapper.Error -> ResultWrapper.Error((results[3] as ResultWrapper.Error).exception)
                    results[4] is ResultWrapper.Error -> ResultWrapper.Error((results[4] as ResultWrapper.Error).exception)
                    results[5] is ResultWrapper.Error -> ResultWrapper.Error((results[5] as ResultWrapper.Error).exception)

                    else -> {
                        try {
                            val user = (results[0] as ResultWrapper.Success<User>).data
                            val posts = (results[1] as ResultWrapper.Success<List<FeedPost>>).data
                            val isFollowing = (results[2] as ResultWrapper.Success<Boolean>).data
                            val localWorkouts = (results[3] as ResultWrapper.Success<List<Workout>>).data
                            val workoutLikes = results[4] as List<CachedLike?>
                            val exerciseList = (results[5] as ResultWrapper.Success<List<Exercise>>).data

                            val processedData = ProcessedProfileData(
                                user = user,
                                posts = posts,
                                isFollowing = isFollowing,
                                exerciseList = exerciseList,
                                workouts = localWorkouts.map { workout ->
                                    WorkoutWithStatus(
                                        workout = workout,
                                        isLiked = workoutLikes.any { like ->
                                            like?.targetId == workout.id && like.isLiked
                                        },
                                        isSaved = true
                                    )
                                }
                            )
                            ResultWrapper.Success(processedData)
                        } catch (e: Exception) {
                            ResultWrapper.Error(e)
                        }
                    }
                }
            }
                .onEach { result ->
                    when (result) {
                        is ResultWrapper.Success -> {
                            _uiData.update { currentState ->
                                currentState.copy(
                                    user = result.data.user,
                                    posts = result.data.posts,
                                    isFollowing = result.data.isFollowing,
                                    exerciseList = result.data.exerciseList,
                                    workoutWithStatusList = result.data.workouts
                                )
                            }
                            _uiState.value = ProfileUiState.Success(
                                if (userId == null) ProfileType.CURRENT_USER else ProfileType.OTHER_USER
                            )
                        }
                        is ResultWrapper.Error -> {
                            _uiState.value = ProfileUiState.Error.StringError(
                                result.exception.message ?: "Unknown error"
                            )
                        }

                        is ResultWrapper.Loading -> _uiState.value = ProfileUiState.Loading
                    }
                }
                .catch { throwable ->
                    _uiState.value = ProfileUiState.Error.StringError(
                        throwable.message ?: "Unknown error"
                    )
                }
                .collect {}
        }
    }


    private data class ProcessedProfileData(
        val user: User,
        val posts: List<FeedPost>,
        val isFollowing: Boolean,
        val exerciseList: List<Exercise>,
        val workouts: List<WorkoutWithStatus>
    )

    fun toggleFollow(userIdToFollowUnfollow: String?) {
        viewModelScope.launch {
            TODO()
        }
    }

    //todo use this
    fun deleteLocalWorkout(workoutId: String) = viewModelScope.launch {
            workoutRepository.deleteWorkoutLocally(workoutId).onSuccess {
                loadLocalWorkouts() // Refresh local workouts after deletion
            }.onError { exception ->
                _uiState.update {
                    ProfileUiState.Error.StringError(
                        exception.message ?: "Unknown error"
                    )
                }
            }
    }

    //todo when user deletes local workout
    private fun loadLocalWorkouts() = viewModelScope.launch {

    }


    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.signOut()) {
                is ResultWrapper.Success -> onLoggedOut()
                is ResultWrapper.Error -> _uiState.update {
                    ProfileUiState.Error.StringError(
                        result.exception.message ?: "Unknown error"
                    )
                }
                is ResultWrapper.Loading -> _uiState.value = ProfileUiState.Loading
            }
        }
    }



    fun onEditProfilePicture(imagePath: String) {
        viewModelScope.launch {
            when (val result = userRepository.updateUserProfileImage(imagePath)) {
                is ResultWrapper.Success -> {
                    _uiData.update { currentState ->
                        currentState.copy(
                            user = currentState.user.copy(profilePictureUrl = result.data)
                        )
                    }
                }

                is ResultWrapper.Error -> _uiState.update {
                    ProfileUiState.Error.StringError(
                        result.exception.message ?: "Unknown error while updating profile picture"
                    )
                }

                is ResultWrapper.Loading -> _uiState.value = ProfileUiState.Loading
            }
        }

    }
}


