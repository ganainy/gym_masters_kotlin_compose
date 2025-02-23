package com.ganainy.gymmasterscompose.ui.screens.workout_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.ui.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.repository.ILikeRepository
import com.ganainy.gymmasterscompose.ui.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.repository.IWorkoutRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import com.ganainy.gymmasterscompose.ui.room.LikeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutListViewModel @Inject constructor(
    private val workoutRepository: IWorkoutRepository,
    private val userRepository: IUserRepository,
    private val likeRepository: ILikeRepository
) : ViewModel() {
    private val _uiData = MutableStateFlow(WorkoutListUiData())
    var uiData = _uiData.asStateFlow()

    init {
        loadWorkouts(_uiData.value.sortType)

        // For local search filtering
        // Transform the uiState data state flow to filter workouts based on the search query.
        // If the search query is empty, return the original data. Otherwise, filter the workouts
        // whose titles contain the search query (case-insensitive).
        uiData = _uiData.asStateFlow()
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


        // Listen for sort changes (require new firebase call)
        viewModelScope.launch {
            _uiData
                .map { it.sortType }
                .distinctUntilChanged()
                .collect { sortType ->
                    loadWorkouts(sortType)
                }
        }


    }


    fun toggleWorkoutLike(workout: Workout) = viewModelScope.launch {
        try {
            val userId = userRepository.getCurrentUserId()
            likeRepository.toggleLike(
                targetId = workout.id,
                type = LikeType.WORKOUT,
                userId = userId,
            )
        } catch (e: Exception) {
            _uiData.update { it.copy(isLoading = false, error = e.message) }
        }
    }


    fun toggleWorkoutSave(workout: Workout) = viewModelScope.launch {
        try {
            val userId = userRepository.getCurrentUserId()
            val result = workoutRepository.toggleWorkoutSave(workout, userId)

            if (result is ResultWrapper.Success) {
                val isSaved = workoutRepository.isWorkoutSavedByUser(
                    workout.id,
                    userId
                ) as? ResultWrapper.Success
                if (isSaved?.data == true) {
                    workoutRepository.saveWorkoutLocally(workout)
                } else {
                    workoutRepository.deleteWorkoutLocally(workout.id)
                }
            }
        } catch (e: Exception) {
            _uiData.update { it.copy(isLoading = false, error = e.message) }
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadWorkouts(sortType: SortType) = viewModelScope.launch {
        try {
            Log.d("WorkoutViewModel", "Starting workout collection")
            _uiData.update { it.copy(isLoading = true, error = null) }

            // Get user ID once
            val userId = userRepository.getCurrentUserId()

            // Get workouts flow and convert to StateFlow
            val workoutsFlow = workoutRepository.getWorkouts(sortType)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = ResultWrapper.Success(emptyList())
                )

            workoutsFlow
                .flatMapLatest { workoutResult ->
                    when (workoutResult) {
                        is ResultWrapper.Success -> processWorkouts(workoutResult.data, userId)
                        is ResultWrapper.Error -> {
                            _uiData.update {
                                it.copy(
                                    isLoading = false,
                                    error = workoutResult.exception.message,
                                    workoutWithStatusList = emptyList()
                                )
                            }
                            flowOf(emptyList())
                        }
                        is ResultWrapper.Loading -> {
                            _uiData.update { it.copy(isLoading = true) }
                            flowOf(emptyList())
                        }
                    }
                }
                .collect { workoutStatusList ->
                    _uiData.update {
                        it.copy(
                            isLoading = false,
                            workoutWithStatusList = workoutStatusList,
                            sortType = sortType
                        )
                    }
                }

        } catch (e: Exception) {
            Log.e("WorkoutViewModel", "Error loading workouts", e)
            _uiData.update {
                it.copy(
                    isLoading = false,
                    error = e.message,
                    workoutWithStatusList = emptyList()
                )
            }
        }
    }

    private fun processWorkouts(workouts: List<Workout>, userId: String): Flow<List<WorkoutWithStatus>> {
        // Handle empty workout list case
        if (workouts.isEmpty()) {
            return flowOf(emptyList())
        }

        // Create flows for each workout's status
        val statusFlows = workouts.map { workout ->
            combine(
                likeRepository.observeLikeStatus(
                    targetId = workout.id,
                    type = LikeType.WORKOUT,
                    userId = userId
                ),
                flow {
                    val isSaved = (workoutRepository.isWorkoutSavedByUser(workout.id, userId)
                            as? ResultWrapper.Success)?.data ?: false
                    emit(isSaved)
                }
            ) { isLiked, isSaved ->
                WorkoutWithStatus(
                    workout = workout,
                    isLiked = isLiked,
                    isSaved = isSaved
                )
            }
        }

        return combine(statusFlows) { it.toList() }
    }




    fun updateSearchQuery(query: String) {
        _uiData.update { it.copy(searchQuery = query) }
    }


    fun retry() {
        loadWorkouts(_uiData.value.sortType)
    }

    fun updateSortPreference(sortType: SortType) {
        _uiData.update { it.copy(sortType = sortType) }
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

