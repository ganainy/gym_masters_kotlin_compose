package com.ganainy.gymmasterscompose.ui.screens.workout_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.ui.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.repository.ILikeRepository
import com.ganainy.gymmasterscompose.ui.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.repository.IWorkoutRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import com.ganainy.gymmasterscompose.ui.repository.onSuccess
import com.ganainy.gymmasterscompose.ui.room.LikeType
import com.ganainy.gymmasterscompose.ui.screens.workout_list.WorkoutWithStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutDetailsViewModel @Inject constructor(
    private val workoutRepository: IWorkoutRepository,
    private val userRepository: IUserRepository,
    private val likeRepository: ILikeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutDetailsUiData())
    var uiState = _uiState.asStateFlow()



    fun observeWorkoutChanges() {
        viewModelScope.launch {
            try {
                workoutRepository.getWorkoutFlow(
                    _uiState.value.workoutWithStatus?.workout?.id
                        ?: throw Exception("workout id is null")
                )
                    .distinctUntilChanged()
                    .filterNotNull()
                    .collect { workoutResult ->
                        workoutResult.onSuccess { workout ->
                            viewModelScope.launch {

                            if (workout != null) {
                                // Collect like status
                                val isLikedFlow = likeRepository.observeLikeStatus(
                                    targetId = workout.id,
                                    type = LikeType.WORKOUT
                                ).first()

                                // Handle the ResultWrapper from isWorkoutSavedByUser
                                    val isSavedResult = workoutRepository.isWorkoutSavedByUser(
                                        workout.id,
                                        userRepository.getCurrentUserId()
                                    )

                                    // Extract the boolean value from ResultWrapper
                                    val isSaved = when (isSavedResult) {
                                        is ResultWrapper.Success -> isSavedResult.data
                                        is ResultWrapper.Error -> false //todo dont like this
                                        is ResultWrapper.Loading -> false //todo dont like this
                                    }

                                    _uiState.update {
                                        it.copy(
                                            workoutWithStatus = WorkoutWithStatus(
                                                workout = workout,
                                                isLiked = isLikedFlow,
                                                isSaved = isSaved
                                            )
                                        )
                                    }

                                }


                            }
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Unknown error occurred")
                }
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
            _uiState.update { it.copy(isLoading = false, error = e.message) }
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
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }


    fun setWorkoutDetails(workout: Workout, isLiked: Boolean, isSaved: Boolean) {
        _uiState.update {
            it.copy(
                workoutWithStatus = WorkoutWithStatus(
                    workout,
                    isLiked,
                    isSaved
                )
            )
        }
    }


}


data class WorkoutDetailsUiData(
    val workoutWithStatus: WorkoutWithStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

