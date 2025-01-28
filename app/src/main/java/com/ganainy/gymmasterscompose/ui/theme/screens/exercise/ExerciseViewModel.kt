package com.ganainy.gymmasterscompose.ui.theme.screens.exercise

import android.app.Application
import androidx.lifecycle.ViewModel
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val application: Application,
    private val authRepository: IAuthRepository,
) : ViewModel() {
    fun setExercise(exercise: Exercise) {
        _uiState.update { it.copy(exercise = exercise, isLoading = false) }
    }

    //save exercise for this user
    fun saveExercise() {
        TODO("Not yet implemented")
    }


    val context = application

    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState = _uiState.asStateFlow()


    val currentUserId: String
        get() = authRepository.getCurrentUserId()




}


data class ExerciseUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val exercise: Exercise? = null,
)