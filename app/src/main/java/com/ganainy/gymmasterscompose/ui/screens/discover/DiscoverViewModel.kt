package com.ganainy.gymmasterscompose.ui.screens.discover

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.models.Follow
import com.ganainy.gymmasterscompose.ui.models.User
import com.ganainy.gymmasterscompose.ui.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.repository.IUsersRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverData(
    val users: List<UserWithFollowStatus> = listOf(),
    val searchQuery: String = "",
    val user: User? = null,
)

data class UserWithFollowStatus(val user: User, val isFollowing: Boolean)

sealed class DiscoverUiState {
    object Success : DiscoverUiState()
    object Loading : DiscoverUiState()
    sealed class Error : DiscoverUiState() {
        data class IntError(val messageStringResource: Int) : Error()
        data class StringError(val message: String) : Error()
    }
}

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val usersRepository: IUsersRepository,
    private val socialRepository: ISocialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Loading)
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

      private val _discoverData = MutableStateFlow(DiscoverData())
    var discoverData: StateFlow<DiscoverData> = _discoverData.asStateFlow()


    init {

        loadDiscoverContent()


        // Transform the discover data state flow to filter users based on the search query.
        // If the search query is empty, return the original data. Otherwise, filter the users
        // whose display names contain the search query (case-insensitive).
        discoverData= _discoverData.asStateFlow()
            .map { data ->
                if (data.searchQuery.isEmpty()) {
                    data
                } else {
                    data.copy(
                        users = data.users.filter { userWithStatus ->
                            userWithStatus.user.displayName.contains(
                                data.searchQuery,
                                ignoreCase = true
                            )
                        }
                    )
                }
            }
            // Share the state flow while subscribed, with an initial value of an empty DiscoverData object.
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                DiscoverData()
            )

    }

    /**
     * Loads the discover content by combining user updates and following updates.
     *
     * This function sets the UI state to Loading, then combines the results of listening for user updates
     * and following updates. It handles each ResultWrapper to extract data or handle errors, and updates
     * the discover data and UI state accordingly.
     */
    private fun loadDiscoverContent() {
        viewModelScope.launch {
            // Set the UI state to Loading
            _uiState.value = DiscoverUiState.Loading

            // Combine the results of listening for user updates and following updates
            combine(
                usersRepository.listenForUsersUpdates(),
                usersRepository.listenForFollowingUpdates(null),
            ) { results: Array<ResultWrapper<*>> ->

                // Extract the results for users and following list
                val users = results[0] as ResultWrapper<List<User>>
                val followingListOfCurrentUser = results[1] as ResultWrapper<List<Follow>>

                // Handle each ResultWrapper to extract data or handle errors
                when {
                    // If there is an error in fetching users, update the UI state with the error message
                    users is ResultWrapper.Error -> {
                        _uiState.update {
                            DiscoverUiState.Error.StringError(
                                users.exception.message ?: "Unknown error"
                            )
                        }
                        return@combine
                    }

                    // If there is an error in fetching the following list, update the UI state with the error message
                    followingListOfCurrentUser is ResultWrapper.Error -> {
                        _uiState.update {
                            DiscoverUiState.Error.StringError(
                                followingListOfCurrentUser.exception.message ?: "Unknown error"
                            )
                        }
                        return@combine
                    }

                    // If both results are successful, update the discover data and UI state
                    else -> {
                        // All results are successful, safely cast and update state
                        _discoverData.update { currentState ->
                            currentState.copy(
                                users = (users as ResultWrapper.Success).data.map { otherUser ->
                                    UserWithFollowStatus(
                                        user = otherUser,
                                        isFollowing = (followingListOfCurrentUser as ResultWrapper.Success).data.any {
                                            it.followedId == otherUser.id &&
                                                    it.followerId == userRepository.getCurrentUserId()
                                        }
                                    )
                                }
                            )
                        }
                        _uiState.value = DiscoverUiState.Success
                    }
                }
            }.collect {}
        }
    }

    fun onSearchQueryChanged(query: String) {
        _discoverData.update { it.copy(searchQuery = query) }
    }


    /**
     * Follows or unfollows a user.
     *
     * This function launches a coroutine to update the follow state of the specified user.
     * It calls the `updateFollowState` method from the `socialRepository` and updates the UI state
     * based on the result of the operation.
     *
     * @param userToFollowUnfollow The user to follow or unfollow.
     */
    fun followUnfollowUser(userToFollowUnfollow: User) {
        viewModelScope.launch {
            try {

                val result = socialRepository.updateFollowState(userToFollowUnfollow.id)
                when (result) {
                    is ResultWrapper.Success -> {
                        Log.d("TAG", "followUnfollowUser: success")
                    }

                    is ResultWrapper.Error -> {
                        _uiState.value =
                            DiscoverUiState.Error.IntError(R.string.error_unfollowing_user)
                    }

                    is ResultWrapper.Loading -> {
                        Log.d("TAG", "followUnfollowUser: loading")
                }
                }

            } catch (e: Exception) {
                _uiState.value = DiscoverUiState.Error.IntError(R.string.error_unfollowing_user)
            }
        }
    }

    fun refresh() {
        loadDiscoverContent()
    }
}