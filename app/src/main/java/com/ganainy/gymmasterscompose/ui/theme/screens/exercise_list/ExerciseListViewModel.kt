package com.ganainy.gymmasterscompose.ui.theme.screens.exercise_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.ui.theme.models.BodyPart
import com.ganainy.gymmasterscompose.ui.theme.models.Equipment
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.TargetMuscle
import com.ganainy.gymmasterscompose.utils.ExerciseDataManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    private val exerciseDataManager: ExerciseDataManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExerciseListUiState())
    val uiState = _uiState.asStateFlow()

    val filteredExercises = uiState.map { state ->
        when (val dataState = state.dataState) {
            is DataState.Success -> exerciseDataManager.filterExercises(
                exercises = dataState.exercises,
                query = state.searchQuery,
                bodyPart = state.filters.activeFilters.bodyPart,
                target = state.filters.activeFilters.targetMuscle,
                equipment = state.filters.activeFilters.equipment
            )
            else -> emptyList()
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(dataState = DataState.Loading) }

            exerciseDataManager.loadExerciseData()
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            filters = FilterState(
                                bodyPartList = result.bodyParts,
                                targetList = result.targets,
                                equipmentList = result.equipment
                            ),
                            dataState = DataState.Success(result.exercises)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(dataState = DataState.Error(error.message ?: "Unknown error"))
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateFilters(newFilters: ActiveFilters) {
        _uiState.update { it.copy(
            filters = it.filters.copy(activeFilters = newFilters)
        )}
    }

    fun clearFilters() {
        _uiState.update { it.copy(
            filters = it.filters.copy(activeFilters = ActiveFilters())
        )}
    }

    fun retry() {
        loadInitialData()
    }
}


data class ExerciseListUiState(
    val searchQuery: String = "",
    val filters: FilterState = FilterState(),
    val dataState: DataState = DataState.Loading
)

data class FilterState(
    val bodyPartList: List<BodyPart> = emptyList(),
    val targetList: List<TargetMuscle> = emptyList(),
    val equipmentList: List<Equipment> = emptyList(),
    val activeFilters: ActiveFilters = ActiveFilters()
)

data class ActiveFilters(
    val bodyPart: BodyPart? = null,
    val targetMuscle: TargetMuscle? = null,
    val equipment: Equipment? = null
)

sealed class DataState {
    object Loading : DataState()
    data class Success(val exercises: List<Exercise>) : DataState()
    data class Error(val message: String) : DataState()
}