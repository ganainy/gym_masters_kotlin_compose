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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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

    val context=application

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
                userRepository.getUserPosts(targetUserId).map { it.getOrNull().orEmpty() },
                socialRepository.getUserFollowing(targetUserId).map { it.getOrNull().orEmpty() },
                socialRepository.getUserFollowers(targetUserId).map { it.getOrNull().orEmpty() },
                socialRepository.isFollowing(targetUserId).map { it.getOrNull() ?: false },

            ) { user: User, posts: List<FeedPost>, following: List<String>, followers: List<String>,isFollowing: Boolean ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoggedInUser = userId == null,
                        user = user,
                        followers = followers,
                        following = following,
                        posts = posts,
                        isFollowing = isFollowing
                    )
                }
            }.collect {}
        }
    }


    fun toggleFollow(userId: String?) {
        viewModelScope.launch {
           TODO()
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut().onSuccess {
                onLoggedOut()
            }.onFailure {
                _uiState.update { it.copy(error = context.getString(R.string.error_logging_out)) }
            }
        }
    }
}


data class ProfileUiState(
    val isLoggedInUser:Boolean = false,
    val user: User = User(),
    val posts: List<FeedPost> = emptyList(),
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val error: String? = null,
    val isFollowing: Boolean = false
)