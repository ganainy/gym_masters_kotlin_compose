package com.ganainy.gymmasterscompose.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.repository.IExerciseRepository
import com.ganainy.gymmasterscompose.ui.repository.onError
import com.ganainy.gymmasterscompose.ui.repository.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val exerciseRepository: IExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeExerciseSaveState()
    }
    fun setExercise(exercise: Exercise) {
        _uiState.update {
            it.copy(
                exercise = exercise,
                isLoading = false
            )
        }
    }

    //save exercise locally for this user
    fun toggleExerciseSave() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val exercise = uiState.value.exercise
            if (exercise != null) {
                //save exercise to user exercises
                exerciseRepository.toggleExerciseSaveLocally(exercise).onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }.onError {
                    _uiState.update { it.copy(isLoading = false, error = it.error) }
                }

            }
        }

    }

    private fun observeExerciseSaveState() {
        viewModelScope.launch {
            uiState.value.exercise?.let {
                exerciseRepository.observeExercise(it.id).collect { result ->
                    result.onSuccess { exercise ->
                            _uiState.update { currentUiState ->
                                currentUiState.copy(
                                    exercise = exercise,
                                    isLoading = false
                                )
                            }
                    }
                    .onError {
                        _uiState.update { it.copy(isLoading = false, error = it.error) }
                    }
            }
        }
    }

}



data class ExerciseUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val exercise: Exercise? = null,
)
}