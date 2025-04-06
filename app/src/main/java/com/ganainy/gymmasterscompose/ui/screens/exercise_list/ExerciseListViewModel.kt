package com.ganainy.gymmasterscompose.ui.screens.exercise_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.prefs.ExerciseDownloadPrefs
import com.ganainy.gymmasterscompose.ui.models.BodyPart
import com.ganainy.gymmasterscompose.ui.models.Equipment
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.repository.IExerciseRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import com.ganainy.gymmasterscompose.utils.CachedExerciseDataResult
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
    private val exerciseRepository: IExerciseRepository,
    private val exerciseDownloadPrefs: ExerciseDownloadPrefs
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

            // Check if initial download is complete
            if (exerciseDownloadPrefs.isInitialDownloadComplete()) {
                Log.d("ExerciseListViewModel", "Initial download complete. Loading from cache.")
                // Load directly from cache using the manager/repository
                exerciseDataManager.loadCachedExerciseData()
                    .onSuccess { result: CachedExerciseDataResult -> // Specify type
                        // Check if cache was actually populated
                        if (result.exercises.isEmpty()) {
                            Log.w("ExerciseListViewModel", "Initial download was marked complete, but cache is empty. Triggering download again.")
                            // Set state to trigger download UI
                            _uiState.update {
                                it.copy(dataState = DataState.InitialDownloadRequired("Cache empty, please download exercises."))
                            }
                        } else {
                            // Cache has data, update UI
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
                    }
                    .onFailure { error ->
                        Log.e("ExerciseListViewModel", "Error loading data from cache", error)
                        _uiState.update {
                            it.copy(dataState = DataState.Error(error.message ?: "Error loading cached data"))
                        }
                    }
            } else {
                // Initial download not yet complete or failed previously
                Log.d("ExerciseListViewModel", "Initial download not complete. Prompting user.")
                _uiState.update {
                    it.copy(dataState = DataState.InitialDownloadRequired("Exercises need to be downloaded."))
                }
            }
        }
    }


    fun retry() {
        // Only retry if in the InitialDownloadRequired state
        if (_uiState.value.dataState is DataState.InitialDownloadRequired) {
            viewModelScope.launch {
                Log.d("ExerciseListViewModel", "Retry button clicked. Forcing exercise download...")
                _uiState.update { it.copy(dataState = DataState.Loading) } // Show loading during download attempt

                val result = exerciseRepository.fetchAllExercisesAndCache(forceRefresh = true)

                when(result) {
                    is ResultWrapper.Success -> {
                        Log.i("ExerciseListViewModel", "Retry download successful. Reloading cached data.")
                        // Now that download succeeded, load data from cache
                        loadInitialData()
                    }
                    is ResultWrapper.Error -> {
                        Log.e("ExerciseListViewModel", "Retry download failed.", result.exception)
                        // Stay in the download required state, update message
                        _uiState.update {
                            it.copy(dataState = DataState.InitialDownloadRequired("Download failed. Please try again."))
                        }
                    }
                    is ResultWrapper.Loading -> { /* Should not happen */ }
                }
            }
        } else {
            // If retry is called in other states (e.g. Error loading cache), just reload cache
            Log.d("ExerciseListViewModel", "Retry called in non-download state. Reloading cached data.")
            loadInitialData()
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
    data class Error(val message: String) : DataState() // Error loading CACHED data
    data class InitialDownloadRequired(val message: String) : DataState() // Cache empty/download failed
}