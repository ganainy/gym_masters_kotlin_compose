package com.ganainy.gymmasterscompose.ui.theme.screens.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.repository.IAuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ProfileUiData(
    val user: User = User(),
    val posts: List<FeedPost> = emptyList(),
    val isFollowing: Boolean = false
)

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

            combine(
                userRepository.getUserFlow(targetUserId),
                userRepository.getUserPosts(targetUserId),
                socialRepository.isFollowing(targetUserId),
            ) { results: Array<ResultWrapper<*>> ->

                val user = results[0] as ResultWrapper<User>
                val posts = results[1] as ResultWrapper<List<FeedPost>>
                val isFollowing = results[2] as ResultWrapper<Boolean>

                // Handle each ResultWrapper to extract data or handle errors
                val profileData = when {
                    user is ResultWrapper.Error -> {
                        _uiState.update {
                            ProfileUiState.Error.StringError(
                                user.exception.message ?: "Unknown error"
                            )
                        }
                        return@combine
                    }

                    posts is ResultWrapper.Error -> {
                        _uiState.update {
                            ProfileUiState.Error.StringError(
                                posts.exception.message ?: "Unknown error"
                            )
                        }
                        return@combine
                    }

                   isFollowing is ResultWrapper.Error -> {
                        _uiState.update {
                            ProfileUiState.Error.StringError(
                                isFollowing.exception.message ?: "Unknown error"
                            )
                        }
                        return@combine
                    }

                    else -> {
                        // All results are successful, safely cast and update state
                        _uiData.update { currentState ->
                            currentState.copy(
                                user = (user as ResultWrapper.Success).data,
                                posts = (posts as ResultWrapper.Success).data,
                                isFollowing = (isFollowing as ResultWrapper.Success).data
                            )
                        }
                        _uiState.value= ProfileUiState.Success(if (userId == null) ProfileType.CURRENT_USER else ProfileType.OTHER_USER)
                    }
                }
            }.collect {}
        }
    }

    fun toggleFollow(userIdToFollowUnfollow: String?) {
        viewModelScope.launch {
            TODO()
        }
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
            }
        }
    }
}


