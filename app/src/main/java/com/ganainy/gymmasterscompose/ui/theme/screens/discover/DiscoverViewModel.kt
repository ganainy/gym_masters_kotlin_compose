package com.ganainy.gymmasterscompose.ui.theme.screens.discover

import User
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUsersRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverData(
    val users: List<User> = listOf(),
    val searchQuery: String = "",
    val user: User? = null,
    val loggedUserFollowingList: List<String>? = null,
)

sealed class DiscoverUiState {
    object Success : DiscoverUiState()
    object Loading : DiscoverUiState()
    data class Error(val messageStringResource: Int) : DiscoverUiState()
}

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: IUserRepository,
    private val usersRepository: IUsersRepository,
    private val socialRepository: ISocialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Loading)
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    // Maintain separate lists for all users and filtered users
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    private val _discoverData = MutableStateFlow(DiscoverData())
    val discoverData: StateFlow<DiscoverData> = _discoverData.asStateFlow()

    // Keep track of the current search query
    private var currentSearchQuery = ""

    init {
        _uiState.value = DiscoverUiState.Loading

        viewModelScope.launch {
            // Setup listeners
            usersRepository.listenForUsersUpdates()
            usersRepository.listenForFollowersUpdates()

            // Collect users updates
            launch {
                userRepository.getAllUsers().collect { users ->
                    _allUsers.value = users
                    // Reapply current search filter when users list updates
                    applySearchFilter(currentSearchQuery)
                    _uiState.value = DiscoverUiState.Success
                }
            }

            // Collect following updates
            launch {
                socialRepository.getUserFollowing(auth.uid)
                    .collect { result ->
                        result.onSuccess { userFollowingList ->
                            _discoverData.update {
                                it.copy(loggedUserFollowingList = userFollowingList)
                            }
                        }.onFailure { exception ->
                            _uiState.value = DiscoverUiState.Error(R.string.error_loading_following)
                        }
                    }
            }
        }
    }

    private fun applySearchFilter(query: String) {
        val filteredUsers = if (query.isEmpty()) {
            _allUsers.value
        } else {
            _allUsers.value.filter { user ->
                user.profile.displayName.contains(query, ignoreCase = true)
            }
        }
        _discoverData.update { it.copy(users = filteredUsers) }
    }

    fun onQueryChange(query: String) {
        currentSearchQuery = query
        applySearchFilter(query)
        _discoverData.update { it.copy(searchQuery = query) }
    }

    fun isFollowedByLoggedUser(userToCheckIfFollowed: User): Boolean {
        return discoverData.value.loggedUserFollowingList?.contains(userToCheckIfFollowed.profile.id)
            ?: false
    }

    fun followUnfollowUser(userToFollowUnfollow: User) {
        viewModelScope.launch {
            try {

                socialRepository.getUserFollowing(userId = auth.uid).collect { result ->
                    result.onSuccess { followingList ->
                        _discoverData.update { it.copy(loggedUserFollowingList = followingList) }
                    }.onFailure { exception ->
                        _uiState.value = DiscoverUiState.Error(R.string.error_loading_following)
                    }
                }

                if (discoverData.value.loggedUserFollowingList?.contains(userToFollowUnfollow.profile.id) == true) {
                    socialRepository.unfollowUser(userToFollowUnfollow.profile.id).onSuccess {
                        _discoverData.update { it.copy(loggedUserFollowingList = it.loggedUserFollowingList?.minus(
                            userToFollowUnfollow.profile.id
                        )) }
                    }
                } else {
                    socialRepository.followUser(userToFollowUnfollow.profile.id).onSuccess {
                        _discoverData.update { it.copy(loggedUserFollowingList = it.loggedUserFollowingList?.plus(userToFollowUnfollow.profile.id)) }
                    }
                }

            } catch (e: Exception) {
                _uiState.value = DiscoverUiState.Error(R.string.error_follow_unfollow)
            }
        }
    }
}