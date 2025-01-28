package com.ganainy.gymmasterscompose.ui.theme.screens.create_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.Constants
import com.ganainy.gymmasterscompose.ui.theme.models.BodyPart
import com.ganainy.gymmasterscompose.ui.theme.models.Equipment
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.theme.models.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.WorkoutExercise
import com.ganainy.gymmasterscompose.ui.theme.repository.IAuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.IWorkoutRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.ResultWrapper
import com.ganainy.gymmasterscompose.utils.ExerciseDataManager
import com.ganainy.gymmasterscompose.utils.Utils.generateRandomId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class ExerciseListDataState {
    object Initial : ExerciseListDataState()
    object Loading : ExerciseListDataState()
    object Success : ExerciseListDataState()
    data class Error(val message: String) : ExerciseListDataState()
}

sealed class WorkoutDataState {
    object Initial : WorkoutDataState()
    object Loading : WorkoutDataState()
    object Success : WorkoutDataState()
    data class Error(val message: String) : WorkoutDataState()
}


data class WorkoutUiState(
    val exerciseListState: ExerciseListDataState = ExerciseListDataState.Initial,
    val workoutState: WorkoutDataState = WorkoutDataState.Initial,
    val searchQuery: String = "",
    val currentTag: String = "",
    val bodyPartFilter: BodyPart? = null,
    val equipmentFilter: Equipment? = null,
    val targetFilter: TargetMuscle? = null,
    val bodyPartList: List<BodyPart> = emptyList(),
    val equipmentList: List<Equipment> = emptyList(),
    val targetList: List<TargetMuscle> = emptyList(),
    val exerciseList: List<Exercise> = emptyList(),
    val availableDifficulties: List<String> = listOf("Beginner", "Intermediate", "Advanced", "Expert"),
    val currentDifficulty: String ="Beginner",
    val filteredExerciseList: List<Exercise> = emptyList(),
    val workoutExerciseList: List<WorkoutExercise> = emptyList(),
    val selectedExercise: WorkoutExercise? = null,
    val workout: Workout = Workout(workoutId = generateRandomId(Constants.WORKOUT))
)

@HiltViewModel
class CreateWorkoutViewModel @Inject constructor(
    private val exerciseDataManager: ExerciseDataManager,
    private val authRepository: IAuthRepository,
    private val workoutRepository: IWorkoutRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            setExerciseListState(ExerciseListDataState.Loading)

            exerciseDataManager.loadExerciseData()
                .onSuccess { result ->
                    _uiState.update { state ->
                        state.copy(
                            exerciseListState = ExerciseListDataState.Success,
                            bodyPartList = result.bodyParts,
                            targetList = result.targets,
                            equipmentList = result.equipment,
                            exerciseList = result.exercises,
                            filteredExerciseList = result.exercises,
                            workout = state.workout.copy(userId = authRepository.getCurrentUserId())
                        )
                    }
                }
                .onFailure { error ->
                    setExerciseListState(ExerciseListDataState.Error(error.message ?: "Unknown error"))
                }
        }
    }

    private fun setExerciseListState(state: ExerciseListDataState) {
        _uiState.update { it.copy(exerciseListState = state) }
    }

    private fun setWorkoutState(state: WorkoutDataState) {
        _uiState.update { it.copy(workoutState = state) }
    }

    fun onQueryChange(query: String) = viewModelScope.launch {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun applyFilters() {
        val state = _uiState.value
        val filteredExercises = state.exerciseList.filter { exercise ->
            val matchesSearch = state.searchQuery.isBlank() ||
                    exercise.name.contains(state.searchQuery, ignoreCase = true)
            val matchesBodyPart = state.bodyPartFilter == null ||
                    exercise.bodyPart == state.bodyPartFilter.name
            val matchesEquipment = state.equipmentFilter == null ||
                    exercise.equipment == state.equipmentFilter.name
            val matchesTarget = state.targetFilter == null ||
                    exercise.target == state.targetFilter.name

            matchesSearch && matchesBodyPart && matchesEquipment && matchesTarget
        }

        _uiState.update { it.copy(filteredExerciseList = filteredExercises) }
    }

    fun uploadWorkout() = viewModelScope.launch {
        try {
            setWorkoutState(WorkoutDataState.Loading)

            when (val result = workoutRepository.uploadWorkout(_uiState.value.workout)) {
                is ResultWrapper.Success -> {
                    setWorkoutState(WorkoutDataState.Success)
                    resetUiState()
                }
                is ResultWrapper.Error -> {
                    setWorkoutState(WorkoutDataState.Error(result.exception.message ?: "Unknown error"))
                }
                else -> Unit
            }
        } catch (e: Exception) {
            setWorkoutState(WorkoutDataState.Error(e.message ?: "Unknown error"))
        }
    }

    fun addWorkoutExercise(newWorkoutExercise: WorkoutExercise?) {
        newWorkoutExercise ?: return
        _uiState.update { state ->
            val updatedList = state.workoutExerciseList.toMutableList()
            val existingIndex = updatedList.indexOfFirst {
                it.exercise.id == newWorkoutExercise.exercise.id
            }

            if (existingIndex != -1) {
                updatedList[existingIndex] = newWorkoutExercise
            } else {
                updatedList.add(newWorkoutExercise)
            }

            state.copy(
                workoutExerciseList = updatedList,
                selectedExercise = null // Clear selection after adding
            )
        }
    }

    fun deleteWorkoutExercise(workoutExercise: WorkoutExercise) {
        _uiState.update { state ->
            state.copy(
                workoutExerciseList = state.workoutExerciseList - workoutExercise,
                selectedExercise = if (state.selectedExercise == workoutExercise) null else state.selectedExercise
            )
        }
    }

    fun setSelectedExercise(exercise: Exercise?) {
        val workoutExercise = exercise?.let { WorkoutExercise(it) }
        _uiState.update { it.copy(selectedExercise = workoutExercise) }
    }

    // Filter Updates
    fun onBodyPartFilterChange(bodyPart: BodyPart?) {
        _uiState.update { it.copy(bodyPartFilter = bodyPart) }
        applyFilters()
    }

    fun onEquipmentFilterChange(equipment: Equipment?) {
        _uiState.update { it.copy(equipmentFilter = equipment) }
        applyFilters()
    }

    fun onTargetMuscleFilterChange(targetMuscle: TargetMuscle?) {
        _uiState.update { it.copy(targetFilter = targetMuscle) }
        applyFilters()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                bodyPartFilter = null,
                equipmentFilter = null,
                targetFilter = null,
                searchQuery = ""
            )
        }
        applyFilters()
    }

    // Workout Updates
    fun updateWorkout(update: (Workout) -> Workout) {
        _uiState.update { state ->
            state.copy(workout = update(state.workout))
        }
    }

    // Tag Management
    fun addTag() {
        val currentTag = _uiState.value.currentTag.trim()
        if (currentTag.isNotBlank()) {
            updateWorkout { it.copy(tags = it.tags + currentTag) }
            _uiState.update { it.copy(currentTag = "") }
        }
    }

    fun removeTag(tag: String) {
        updateWorkout { it.copy(tags = it.tags - tag) }
    }

    fun changeCurrentTagText(tag: String) {
        _uiState.update { it.copy(currentTag = tag) }
    }

    // Reset and Clear functions
    fun resetUiState() {
        _uiState.value = WorkoutUiState().copy(
            workout = Workout(
                workoutId = generateRandomId(Constants.WORKOUT),
                userId = authRepository.getCurrentUserId()
            )
        )
        loadInitialData()
    }

    fun clearError() {
        _uiState.update { state ->
            state.copy(
                exerciseListState = when (state.exerciseListState) {
                    is ExerciseListDataState.Error -> ExerciseListDataState.Initial
                    else -> state.exerciseListState
                },
                workoutState = when (state.workoutState) {
                    is WorkoutDataState.Error -> WorkoutDataState.Initial
                    else -> state.workoutState
                }
            )
        }
    }

    fun retry() {
        loadInitialData()
    }

    fun editWorkoutExercise(workoutExercise: WorkoutExercise?) {
        _uiState.update { it.copy(selectedExercise = workoutExercise) }
    }

    fun editWorkout(workout: Workout) {
        _uiState.update { it.copy(workout = workout) }
    }

    fun changeWorkoutDifficulty(newDifficulty: String) {
        _uiState.update { it.copy(currentDifficulty = newDifficulty) }
    }


}