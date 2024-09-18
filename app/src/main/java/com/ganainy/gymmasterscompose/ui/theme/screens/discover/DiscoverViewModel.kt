package com.ganainy.gymmasterscompose.ui.theme.screens.discover

import LocalUser
import User
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.repository.IDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class DiscoverData(
    val users: List<LocalUser> = listOf(),
    val searchQuery: String = "",
    val user: User? = null,
    val userFollowingMap: Map<String, String>? = null,
)

sealed class DiscoverUiState {
    object Success : DiscoverUiState()
    object Loading : DiscoverUiState()
    data class Error(val messageStringResource: Int) : DiscoverUiState()
}


class DiscoverViewModel(private val dataRepository: IDataRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Loading)
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    // Holds the original list of all users
    private val allUsers = mutableListOf<LocalUser>()

    private val _discoverData = MutableStateFlow(DiscoverData())
    val discoverData: StateFlow<DiscoverData> = _discoverData.asStateFlow()

    init {

        _uiState.value = DiscoverUiState.Loading
        // Load all users when the ViewModel is created
        viewModelScope.launch {

            dataRepository.listenForUsersUpdates()
            dataRepository.listenForFollowersUpdates()

            dataRepository.getUsers().collect { users ->
                allUsers.clear()  // Clear any previous data
                allUsers.addAll(users)  // Cache the full list of users
                _discoverData.update { it.copy(users = users) }  // Show all users initially
                _uiState.value = DiscoverUiState.Success
            }


        }

        // listen to logged user updates
        /*dataRepository.getLoggedUser { result ->
            result.onSuccess { updatedUser ->
                _discoverData.update {
                    it.copy(
                        user = updatedUser
                    )
                }
            }.onFailure { exception ->
                val cExeption = exception as CustomException
                _uiState.value = DiscoverUiState.Error(cExeption.stringRes)
            }
        }*/
        // listen to logged user following list updates
        dataRepository.getUserFollowing(onSuccess = { userFollowingMap ->
            _discoverData.update {
                it.copy(userFollowingMap = userFollowingMap)
            }
        }, onFailure = {
            _uiState.value = DiscoverUiState.Error(it)
        })
    }

    fun isFollowedByLoggedUser(userToCheckIfFollowed: User): Boolean {
        return discoverData.value.userFollowingMap?.containsValue(userToCheckIfFollowed.userId)
            ?: false
    }


    // Update search query and filter the users
    fun onQueryChange(query: String) {
        _discoverData.update { currentState ->
            // Filter users based on the query (case-insensitive search)
            val filteredUsers = if (query.isEmpty()) {
                allUsers // If query is empty, show all users
            } else {
                allUsers.filter { localUser ->
                    localUser.user?.name?.contains(query, ignoreCase = true)
                        ?: false // Filter based on the user's name
                }
            }
            currentState.copy(searchQuery = query, users = filteredUsers)
        }
    }

    fun followUnfollowUser(userToFollowUnfollow: User) {
        // Launch a coroutine in the viewModelScope
        viewModelScope.launch {
            try {
                // Call the suspend function
                dataRepository.followUnfollowUser(
                    userToFollowUnfollow,
                    onSuccess = {
                    },
                    onFailure = { messageStringResource ->
                        DiscoverUiState.Error(messageStringResource)
                    })
            } catch (e: Exception) {
                _uiState.value = DiscoverUiState.Error(R.string.error_follow_unfollow)
            }
        }
    }

}


class DiscoverViewModelFactory(private val repository: IDataRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiscoverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiscoverViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}