package com.ganainy.gymmasterscompose.ui.theme.screens.workout_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IWorkoutRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutListViewModel @Inject constructor(
private val workoutRepository: IWorkoutRepository,private val userRepository: IUserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutListUiData())
    var uiState = _uiState.asStateFlow()

    init {
        loadWorkouts(_uiState.value.sortType)

        // Transform the uiState data state flow to filter workouts based on the search query.
        // If the search query is empty, return the original data. Otherwise, filter the workouts
        // whose titles contain the search query (case-insensitive).
        uiState= _uiState.asStateFlow()
            .map { data ->
                if (data.searchQuery.isEmpty()) {
                    data
                } else {
                    data.copy(
                        workoutWithStatusList = data.workoutWithStatusList.filter { workoutWithStatus ->
                            workoutWithStatus.workout.title.contains(
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
                WorkoutListUiData()
            )
    }



    fun toggleWorkoutLike(workout: Workout) = viewModelScope.launch {
        try {
            _uiState.update { it.copy(isLoading = true) }
            val userId = userRepository.getCurrentUserId()
            workoutRepository.toggleWorkoutLike(workout, userId)
            refreshWorkoutStatus(workout.id)
            _uiState.update { it.copy(isLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }



    fun toggleWorkoutSave(workout: Workout) = viewModelScope.launch {
        try {
            _uiState.update { it.copy(isLoading = true) }
            val userId = userRepository.getCurrentUserId()
            val result = workoutRepository.toggleWorkoutSave(workout, userId)

            if (result is ResultWrapper.Success) {
                val isSaved = workoutRepository.isWorkoutSavedByUser(workout.id, userId) as? ResultWrapper.Success
                if (isSaved?.data == true) {
                    workoutRepository.saveWorkoutLocally(workout)
                } else {
                    workoutRepository.deleteWorkoutLocally(workout.id)
                }
            }

            refreshWorkoutStatus(workout.id)
            _uiState.update { it.copy(isLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }


    fun loadWorkouts(sortType: SortType) = viewModelScope.launch {
        try {
            _uiState.update { it.copy(isLoading = true) }
            workoutRepository.getWorkouts(sortType).collect { workoutList ->

                if (workoutList is ResultWrapper.Success) {
                    val workouts = workoutList.data
                    val userId = userRepository.getCurrentUserId()
                    val workoutWithStatusList = workouts.map { workout ->
                        val isLikedDeferred = async { workoutRepository.isWorkoutLikedByUser(workout.id, userId) }
                        val isSavedDeferred = async { workoutRepository.isWorkoutSavedByUser(workout.id, userId) }
                        val isLiked = (isLikedDeferred.await() as? ResultWrapper.Success)?.data ?: false
                        val isSaved = (isSavedDeferred.await() as? ResultWrapper.Success)?.data ?: false
                        WorkoutWithStatus(workout, isLiked, isSaved)
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            workoutWithStatusList = workoutWithStatusList,
                            sortType = sortType
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, workoutWithStatusList = emptyList(), sortType = sortType) }
                }
            }

        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    //todo move to workoutDetails screen for when user deletes local workout
    fun deleteLocalWorkout(workoutId: String) = viewModelScope.launch {
        try {
            _uiState.update { it.copy(isLoading = true) }
            workoutRepository.deleteWorkoutLocally(workoutId)
            loadLocalWorkouts() // Refresh local workouts after deletion
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }



    fun loadLocalWorkouts() = viewModelScope.launch {
        try {
            _uiState.update { it.copy(isLoading = true) }
            val workouts = workoutRepository.getLocalWorkouts()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    localWorkouts = (workouts as? ResultWrapper.Success)?.data ?: emptyList()
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }


    private suspend fun refreshWorkoutStatus(workoutId: String) {
        try {
            val userId = userRepository.getCurrentUserId()

            val isLiked = workoutRepository.isWorkoutLikedByUser(workoutId, userId)
            val isSaved = workoutRepository.isWorkoutSavedByUser(workoutId, userId)

            _uiState.update { state ->
                state.copy(
                    workoutWithStatusList = state.workoutWithStatusList.map { workoutWithStatus ->
                        if (workoutWithStatus.workout.id == workoutId) {
                            workoutWithStatus.copy(
                                isLiked = (isLiked as? ResultWrapper.Success)?.data ?: false,
                                isSaved = (isSaved as? ResultWrapper.Success)?.data ?: false
                            )
                        } else {
                            workoutWithStatus
                        }
                    }
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }


    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }


    fun retry() {
        loadWorkouts(_uiState.value.sortType)
    }

    fun updateSortPreference(sortType: SortType) {
        _uiState.update { it.copy(sortType = sortType) }
    }
}

/*
* Data class to store the status of a workout (liked, saved) along with the workout object.
*/
data class WorkoutWithStatus(
    val workout: Workout,
    val isLiked: Boolean,
    val isSaved: Boolean
)

enum class SortType {
    NEWEST,
    MOST_LIKED,
    MOST_SAVED
}


data class WorkoutListUiData(
    val workoutWithStatusList: List<WorkoutWithStatus> = emptyList(),
    val localWorkouts: List<Workout> = emptyList(),
    val searchQuery: String = "",
    val sortType: SortType = SortType.NEWEST,
    val isLoading: Boolean = false,
    val error: String? = null
)

