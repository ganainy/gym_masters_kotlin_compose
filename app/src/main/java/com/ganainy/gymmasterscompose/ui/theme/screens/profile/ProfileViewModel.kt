package com.ganainy.gymmasterscompose.ui.theme.screens.profile

import User
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.FeedPost
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

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val application: Application,
    private val userRepository: IUserRepository,
    private val authRepository: IAuthRepository,
    private val socialRepository: ISocialRepository,
) : ViewModel() {

    val context = application

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()


    val currentUserId: String
        get() = authRepository.getCurrentUserId()


    //if no userId is provided, the current user's profile is loaded otherwise the profile of the user with the provided id
    fun loadProfile(userId: String?) {
        viewModelScope.launch {
            val targetUserId = userId ?: currentUserId

            combine(
                userRepository.getUser(targetUserId),
                userRepository.getUserPosts(targetUserId),
                socialRepository.getUserFollowing(targetUserId),
                socialRepository.getUserFollowers(targetUserId),
                socialRepository.isFollowing(targetUserId),
            ) { user: ResultWrapper<User>,
                posts: ResultWrapper<List<FeedPost>>,
                following: ResultWrapper<List<String>>,
                followers: ResultWrapper<List<String>>,
                isFollowing: ResultWrapper<Boolean> ->

                // Handle each ResultWrapper to extract data or handle errors
                val profileData = when {
                    user is ResultWrapper.Error -> {
                        _uiState.update { it.copy(error = user.exception.message) }
                        return@combine
                    }
                    posts is ResultWrapper.Error -> {
                        _uiState.update { it.copy(error = posts.exception.message) }
                        return@combine
                    }
                    following is ResultWrapper.Error -> {
                        _uiState.update { it.copy(error = following.exception.message) }
                        return@combine
                    }
                    followers is ResultWrapper.Error -> {
                        _uiState.update { it.copy(error = followers.exception.message) }
                        return@combine
                    }
                    isFollowing is ResultWrapper.Error -> {
                        _uiState.update { it.copy(error = isFollowing.exception.message) }
                        return@combine
                    }
                    else -> {
                        // All results are successful, safely cast and update state
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoggedInUser = userId == null,
                                user = (user as ResultWrapper.Success).data,
                                followers = (followers as ResultWrapper.Success).data,
                                following = (following as ResultWrapper.Success).data,
                                posts = (posts as ResultWrapper.Success).data,
                                isFollowing = (isFollowing as ResultWrapper.Success).data,
                                error = null
                            )
                        }
                    }
                }
            }.collect{}
        }
    }


    fun toggleFollow(userId: String?) {
        viewModelScope.launch {
            TODO()
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.signOut()) {
                is ResultWrapper.Success -> onLoggedOut()
                is ResultWrapper.Error -> _uiState.update { it.copy(error = context.getString(R.string.error_logging_out)) }
            }
        }
    }
}


data class ProfileUiState(
    val isLoggedInUser: Boolean = false,
    val user: User = User(),
    val posts: List<FeedPost> = emptyList(),
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val error: String? = null,
    val isFollowing: Boolean = false
)