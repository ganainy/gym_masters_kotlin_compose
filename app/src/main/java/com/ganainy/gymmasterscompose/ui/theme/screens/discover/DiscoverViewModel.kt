package com.ganainy.gymmasterscompose.ui.theme.screens.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.repository.IDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class DiscoverData(
    val users: List<User> = listOf(),
    val searchQuery: String = "",
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
    private val allUsers = mutableListOf<User>()

    private val _discoverData = MutableStateFlow(DiscoverData())
    val discoverData: StateFlow<DiscoverData> = _discoverData.asStateFlow()

    init {
        // Load all users when the ViewModel is created
        loadAllUsers()
    }

    // Load all users except logged-in user to show in discover screen
    private fun loadAllUsers() {
        dataRepository.loadUsers(onSuccess = { users ->
            setExercisesCount()
            setWorkoutsCount()


            allUsers.clear()  // Clear any previous data
            allUsers.addAll(users)  // Cache the full list of users
            _discoverData.update { it.copy(users = users) }  // Show all users initially
            _uiState.value = DiscoverUiState.Success
        },
            onFailure = { error -> _uiState.value = DiscoverUiState.Error(error) })
    }

    // Get the exercises count for each user
    private fun setExercisesCount() {
        dataRepository.setExercisesCount(onSuccess = { users ->
            _discoverData.update { it.copy(users = users) }
        }, onFailure = { error ->
            _uiState.value = DiscoverUiState.Error(error)
        })
    }

    // Get the workouts count for each user
    private fun setWorkoutsCount() {
        dataRepository.setWorkoutsCount(onSuccess = { users ->
            _discoverData.update { it.copy(users = users) }
        }, onFailure = { error ->
            _uiState.value = DiscoverUiState.Error(error)
        })
    }

    // Update search query and filter the users
    fun onQueryChange(query: String) {
        _discoverData.update { currentState ->
            // Filter users based on the query (case-insensitive search)
            val filteredUsers = if (query.isEmpty()) {
                allUsers // If query is empty, show all users
            } else {
                allUsers.filter { user ->
                    user.name?.contains(query, ignoreCase = true)
                        ?: false // Filter based on the user's name
                }
            }
            currentState.copy(searchQuery = query, users = filteredUsers)
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