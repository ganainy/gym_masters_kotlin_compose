package com.ganainy.gymmasterscompose.ui.theme.screens.workout_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.repository.ILikeRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IWorkoutRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ResultWrapper
import com.ganainy.gymmasterscompose.ui.theme.room.LikeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
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
            _uiData.update { it.copy(isLoading = true) }

            // Convert the workouts flow to a StateFlow so we can combine it with other flows
            val workoutsFlow = workoutRepository.getWorkouts(sortType)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = ResultWrapper.Success(emptyList())
                )

            // Get user ID once
            val userId = userRepository.getCurrentUserId()

            // Combine everything into a single flow
            workoutsFlow
                .flatMapLatest { workoutResult ->
                    if (workoutResult !is ResultWrapper.Success) {
                        flow {
                            emit(emptyList<WorkoutWithStatus>())
                        }
                    } else {
                        val workouts = workoutResult.data

                        // Create flows for each workout's like status
                        val statusFlows = workouts.map { workout ->
                            val isLikedFlow = likeRepository.observeLikeStatus(
                                targetId = workout.id,
                                type = LikeType.WORKOUT,
                                userId = userId
                            )

                            // Combine the like status with the current workout
                            isLikedFlow.map { isLiked ->
                                val currentWorkout = workouts.find { it.id == workout.id } ?: workout
                                async {
                                    val isSaved = (workoutRepository.isWorkoutSavedByUser(
                                        currentWorkout.id,
                                        userId
                                    ) as? ResultWrapper.Success)?.data ?: false

                                    WorkoutWithStatus(
                                        workout = currentWorkout,
                                        isLiked = isLiked,
                                        isSaved = isSaved
                                    )
                                }
                            }
                        }

                        combine(statusFlows) { deferredList ->
                            deferredList.map { it.await() }
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
            _uiData.update { it.copy(isLoading = false, error = e.message) }
        }
    }


    //todo move to workoutDetails screen for when user deletes local workout
    fun deleteLocalWorkout(workoutId: String) = viewModelScope.launch {
        try {
            _uiData.update { it.copy(isLoading = true) }
            workoutRepository.deleteWorkoutLocally(workoutId)
            loadLocalWorkouts() // Refresh local workouts after deletion
        } catch (e: Exception) {
            _uiData.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    //todo move to workoutDetails screen for when user deletes local workout
    fun loadLocalWorkouts() = viewModelScope.launch {
        try {
            _uiData.update { it.copy(isLoading = true) }
            val workouts = workoutRepository.getLocalWorkouts()
            _uiData.update {
                it.copy(
                    isLoading = false,
                    localWorkouts = (workouts as? ResultWrapper.Success)?.data ?: emptyList()
                )
            }
        } catch (e: Exception) {
            _uiData.update { it.copy(isLoading = false, error = e.message) }
        }
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

