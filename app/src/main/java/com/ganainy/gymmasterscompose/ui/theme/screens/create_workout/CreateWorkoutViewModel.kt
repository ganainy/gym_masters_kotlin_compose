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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


// State Management
sealed interface UiState {
    sealed interface DataState {
        object Initial : DataState
        object Loading : DataState
        object Success : DataState
        data class Error(val message: String) : DataState
    }

    data class WorkoutUiState(
        val exerciseListState: DataState = DataState.Initial,
        val workoutState: DataState = DataState.Initial,
        val searchQuery: String = "",
        val currentTag: String = "",
        val bodyPartFilter: BodyPart? = null,
        val equipmentFilter: Equipment? = null,
        val targetFilter: TargetMuscle? = null,
        val bodyPartList: List<BodyPart> = emptyList(),
        val equipmentList: List<Equipment> = emptyList(),
        val targetList: List<TargetMuscle> = emptyList(),
        val exerciseList: List<Exercise> = emptyList(), //all exercises from api or local db
        val availableDifficulties: List<String> = listOf(
            "Beginner",
            "Intermediate",
            "Advanced",
            "Expert"
        ),
        val filteredExerciseList: List<Exercise> = emptyList(),
        val selectedExercise: WorkoutExercise? = null,
        var workout: Workout = Workout(
            workoutId = generateRandomId(Constants.WORKOUT),
            difficulty = "Beginner"
        ),
        val validationState: ValidationState = ValidationState(),
        val showExerciseDialog: Boolean = false,
        val isUploadEnabled: Boolean = false
    )

    data class ValidationState(
        val setsError: String? = null,
        val repsError: String? = null,
        val restError: String? = null
    ) {
        val hasErrors: Boolean
            get() = setsError != null || repsError != null || restError != null
    }
}

// Validator
object WorkoutValidator {
    sealed class ValidationRule<T> {
        abstract fun validate(value: T): String?

        class NonZero : ValidationRule<Int>() {
            override fun validate(value: Int) =
                if (value == 0) "Value cannot be empty" else null
        }
    }

    private val rules = mapOf(
        "sets" to ValidationRule.NonZero(),
        "reps" to ValidationRule.NonZero(),
        "rest" to ValidationRule.NonZero()
    )

    fun validate(workoutExercise: WorkoutExercise): UiState.ValidationState {
        return UiState.ValidationState(
            setsError = rules["sets"]?.validate(workoutExercise.sets),
            repsError = rules["reps"]?.validate(workoutExercise.reps),
            restError = rules["rest"]?.validate(workoutExercise.restBetweenSets)
        )
    }
}

// ViewModel
@HiltViewModel
class CreateWorkoutViewModel @Inject constructor(
    private val exerciseDataManager: ExerciseDataManager,
    private val authRepository: IAuthRepository,
    private val workoutRepository: IWorkoutRepository,
) : ViewModel() {



    /**
     * Private state flow for the UI state.
     */
    private val _uiState = MutableStateFlow(UiState.WorkoutUiState())

    val uiState: StateFlow<UiState.WorkoutUiState> = _uiState.asStateFlow()



    init {
        loadInitialData()
        observeFieldChanges()
    }

    private fun observeFieldChanges() {
        // Combine the two fields into a StateFlow
        uiState.map { state -> state.workout.title to state.workout.workoutExerciseList }
            .distinctUntilChanged() // Only emit when the pair of values changes
            .onEach { (field1, field2) ->
                // Call the function whenever these fields change
                isUploadButtonEnabled()
            }
            .launchIn(viewModelScope) // Replace with your scope (e.g., lifecycleScope)
    }

    /**
     * Updates the UI state to set the upload button to enabled or disabled.
     */
    private fun isUploadButtonEnabled() {
        if (_uiState.value.workout.workoutExerciseList.isNotEmpty() && _uiState.value.workout.title.isNotEmpty()) {
            _uiState.update { it.copy(isUploadEnabled = true) }
        } else {
            _uiState.update { it.copy(isUploadEnabled = false) }
        }
    }


    // Operations Management
    private sealed class Operation {
        object UploadWorkout : Operation()
        object LoadInitialData : Operation()
    }

    private fun handleOperation(operation: Operation, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                setStateForOperation(operation, UiState.DataState.Loading)
                block()
                setStateForOperation(operation, UiState.DataState.Success)
            } catch (e: Exception) {
                setStateForOperation(
                    operation,
                    UiState.DataState.Error(e.message ?: "Unknown error")
                )
            }
        }
    }

    private fun setStateForOperation(operation: Operation, state: UiState.DataState) {
        _uiState.update { currentState ->
            when (operation) {
                is Operation.UploadWorkout -> currentState.copy(workoutState = state)
                is Operation.LoadInitialData -> currentState.copy(exerciseListState = state)
            }
        }
    }

    // Data Loading
    private fun loadInitialData() = handleOperation(Operation.LoadInitialData) {
        exerciseDataManager.loadExerciseData()
            .onSuccess { result ->
                _uiState.update { state ->
                    state.copy(
                        bodyPartList = result.bodyParts,
                        targetList = result.targets,
                        equipmentList = result.equipment,
                        exerciseList = result.exercises,
                        filteredExerciseList = result.exercises,
                        workout = state.workout.copy(userId = authRepository.getCurrentUserId())
                    )
                }
            }
            .onFailure { throw it }
    }

    fun retry() {
        loadInitialData()
    }

    // Exercise Management
    inner class ExerciseManager {
        fun addWorkoutExercise(newWorkoutExercise: WorkoutExercise?) {
            newWorkoutExercise ?: return

            val validationState = WorkoutValidator.validate(newWorkoutExercise)
            if (validationState.hasErrors) {
                _uiState.update { it.copy(validationState = validationState) }
                return
            }

            _uiState.update { state ->

                newWorkoutExercise.order = state.workout.workoutExerciseList.size + 1

                state.copy(
                    selectedExercise = null,
                    showExerciseDialog = false,
                    validationState = UiState.ValidationState(),
                    workout = state.workout.copy(
                        workoutExerciseList = state.workout.workoutExerciseList + newWorkoutExercise
                    ),
                )
            }
        }

        fun deleteWorkoutExercise(workoutExercise: WorkoutExercise) =
            _uiState.update { state ->
                state.copy(
                    workout = state.workout.copy(
                        workoutExerciseList = state.workout.workoutExerciseList - workoutExercise
                    ),
                    selectedExercise = if (state.selectedExercise == workoutExercise) null else state.selectedExercise
                )
            }

        fun setSelectedExercise(exercise: Exercise?) {
            val workoutExercise = exercise?.let { WorkoutExercise(it) }
            _uiState.update {
                it.copy(
                    selectedExercise = workoutExercise,
                    showExerciseDialog = true
                )
            }
        }

        fun editWorkoutExercise(workoutExercise: WorkoutExercise?) {
            _uiState.update { it.copy(selectedExercise = workoutExercise) }
        }

        fun dismissAddExerciseDialog() {
            _uiState.update { it.copy(showExerciseDialog = false) }
        }
    }

    // Filter Management
    inner class FilterManager {
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


    }

    // Tag Management
    inner class TagManager {
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

        private fun updateWorkout(update: (Workout) -> Workout) {
            _uiState.update { it.copy(workout = update(it.workout)) }
        }
    }

    // Workout Management
    inner class WorkoutManager {
        fun uploadWorkout() = handleOperation(Operation.UploadWorkout) {
            val workout = _uiState.value.workout
            val resultImage = workoutRepository.uploadWorkoutCoverImage(workout.imagePath)
            val workoutWithImageUrl = when (resultImage) {
                is ResultWrapper.Success<*> -> workout.copy(imageUrl = (resultImage as ResultWrapper.Success<String>).data)
                else -> workout
            }

            val resultWorkout = workoutRepository.uploadWorkout(workoutWithImageUrl)
            when (resultWorkout) {
                is ResultWrapper.Success -> resetUiState()
                is ResultWrapper.Error -> throw resultWorkout.exception
                else -> Unit
            }
        }

        fun updateWorkout(update: (Workout) -> Workout) {
            _uiState.update { state ->
                state.copy(workout = update(state.workout))
            }
        }

        fun editWorkout(workout: Workout) {
            _uiState.update { it.copy(workout = workout) }
        }


    }

    // State Management
    fun clearError() {
        _uiState.update { state ->
            state.copy(
                exerciseListState = UiState.DataState.Initial,
                workoutState = UiState.DataState.Initial
            )
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.WorkoutUiState().copy(
            workout = Workout(
                workoutId = generateRandomId(Constants.WORKOUT),
                userId = authRepository.getCurrentUserId()
            )
        )
        loadInitialData()
    }

    // Public instance of managers
    val exerciseManager = ExerciseManager()
    val filterManager = FilterManager()
    val tagManager = TagManager()
    val workoutManager = WorkoutManager()
}