package com.ganainy.gymmasterscompose.ui.screens.create_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.Constants
import com.ganainy.gymmasterscompose.ui.models.BodyPart
import com.ganainy.gymmasterscompose.ui.models.Equipment
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.ui.repository.IAuthRepository
import com.ganainy.gymmasterscompose.ui.repository.IWorkoutRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import com.ganainy.gymmasterscompose.utils.ExerciseDataManager
import com.ganainy.gymmasterscompose.utils.Utils.extractHashtags
import com.ganainy.gymmasterscompose.utils.Utils.generateRandomId
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


// State Management
sealed interface UiState {
    sealed interface DataState {
        object Loading : DataState
        object WorkoutSetup : DataState
        object ExerciseListSetup : DataState
        data class Error(val message: String) : DataState
    }

    data class WorkoutUiState(
        val workoutState: DataState = DataState.Loading,
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
            id = generateRandomId(Constants.WORKOUT),
            difficulty = "Beginner"
        ),
        val validationState: ValidationState = ValidationState(),
        val showExerciseDialog: Boolean = false,
        val isUploadEnabled: Boolean = false,
        val showFilterSheet: Boolean = false,
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
        observeWorkoutFieldChanges()
    }

    /**
     * Observes changes in the workout fields and updates the UI state accordingly.
     *
     * This function monitors the `uiState` for changes. It computes whether the upload button should be enabled
     * based on the workout title and the list of workout exercises. It also updates the workout tags by extracting
     * hashtags from both the workout title and description. The updated values are then applied to the UI state.
     */
    private fun observeWorkoutFieldChanges() {
        uiState
            .onEach { state ->
                // Compute whether the upload is enabled
                val isUploadEnabled = state.workout.title.isNotEmpty() &&
                        state.workout.workoutExerciseList.isNotEmpty()

                // Update tags by extracting hashtags from both title and description
                val updatedWorkout = state.workout.copy(
                    tags = (extractHashtags(state.workout.title) +
                            extractHashtags(state.workout.description)).distinct()

                )

                // Update the UI state with both computed values
                _uiState.update { currentState ->
                    currentState.copy(
                        isUploadEnabled = isUploadEnabled,
                        workout = updatedWorkout
                    )
                }
            }
            .launchIn(viewModelScope)
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
                setStateForOperation(operation, UiState.DataState.WorkoutSetup)
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
                is Operation.LoadInitialData -> currentState.copy(workoutState = state)
            }
        }
    }

    // Data Loading
    private fun loadInitialData() = handleOperation(Operation.LoadInitialData) {
        exerciseDataManager.loadCachedExerciseData()
            .onSuccess { result ->
                _uiState.update { state ->
                    state.copy(
                        bodyPartList = result.bodyParts,
                        targetList = result.targets,
                        equipmentList = result.equipment,
                        exerciseList = result.exercises,
                        filteredExerciseList = result.exercises,
                        workout = state.workout.copy(creatorId = authRepository.getCurrentUserId())
                    )
                }
            }
            .onFailure { throw it }
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
                    // delete exercise and update the order of the remaining exercises
                    workout = state.workout.copy(
                        workoutExerciseList = state.workout.workoutExerciseList
                            .filter { it != workoutExercise }
                            .mapIndexed { index, exercise ->
                                exercise.copy(order = index + 1)
                            }
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

        fun hideFilterSheet() {
            _uiState.update { it.copy(showFilterSheet = false) }
        }

        fun showFilterSheet() {
            _uiState.update { it.copy(showFilterSheet = true) }
        }


    }


    // Workout Management
    inner class WorkoutManager {

        fun uploadWorkout(onSuccess: () -> Unit) = handleOperation(Operation.UploadWorkout) {
            val workout = _uiState.value.workout.copy(
                dateCreated = Timestamp.now()
            )
            val result = workoutRepository.uploadWorkoutWithImage(workout, workout.imagePath)

            when (result) {
                is ResultWrapper.Success -> {
                    resetUiState()
                    onSuccess()
                }
                is ResultWrapper.Error -> throw result.exception
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



    fun resetUiState() {
        _uiState.value = UiState.WorkoutUiState().copy(
            workout = Workout(
                id = generateRandomId(Constants.WORKOUT),
                creatorId = authRepository.getCurrentUserId()
            )
        )
        loadInitialData()
    }

    // toggle between setup workout general settings page and exercise list page
    fun toggleExerciseWorkoutListShow() {
        _uiState.update { state ->
            state.copy(
                workoutState =
                    if (state.workoutState == UiState.DataState.ExerciseListSetup) {
                        UiState.DataState.WorkoutSetup
                    } else {
                        UiState.DataState.ExerciseListSetup
                    }
            )
        }
    }

    // Public instance of managers
    val exerciseManager = ExerciseManager()
    val filterManager = FilterManager()
    val workoutManager = WorkoutManager()
}