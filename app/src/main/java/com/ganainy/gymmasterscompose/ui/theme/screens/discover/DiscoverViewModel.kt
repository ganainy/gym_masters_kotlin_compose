package com.ganainy.gymmasterscompose.ui.theme.screens.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.repository.IDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class DiscoverData(
    val users: List<User> = listOf(),
)

sealed class DiscoverUiState {
    object Success : DiscoverUiState()
    object Loading : DiscoverUiState()
    data class Error(val messageStringResource: Int) : DiscoverUiState()
}


class DiscoverViewModel(private val dataRepository: IDataRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Loading)
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private val _discoverData = MutableStateFlow(DiscoverData())
    val discoverData: StateFlow<DiscoverData> = _discoverData.asStateFlow()

    init {
        // Load all users when the ViewModel is created
        loadAllUsers()

        // Observe changes to the 'users' field within 'discoverData'
        viewModelScope.launch {
            _discoverData
                .map { it.users } // Extract the 'users' field
                .distinctUntilChanged() // Only react to actual changes
                .collect {
                    // Call Firebase function when 'users' changes
                    setExercisesCount()
                    setWorkoutsCount()
                }
        }
    }

    //load all users except logged in user to show in discover screen
    private fun loadAllUsers() {
        dataRepository.loadUsers(onSuccess = { users ->
            _discoverData.update { it.copy(users = users) }
            _uiState.value = DiscoverUiState.Success
        },
            onFailure = { error -> _uiState.value = DiscoverUiState.Error(error) })
    }

    // get the exercises count for each user
    private fun setExercisesCount() {
        dataRepository.setExercisesCount(onSuccess = { users ->
            _discoverData.update { it.copy(users = users) }
        }, onFailure = { error ->
            _uiState.value = DiscoverUiState.Error(error)
        })
    }

    //get the workouts count for each user
    private fun setWorkoutsCount() {
        dataRepository.setWorkoutsCount(onSuccess = { users ->
            _discoverData.update { it.copy(users = users) }
        }, onFailure = { error ->
            _uiState.value = DiscoverUiState.Error(error)
        })
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