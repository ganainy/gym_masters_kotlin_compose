package com.ganainy.gymmasterscompose.ui.screens.create_workout

import com.ganainy.gymmasterscompose.ui.models.BodyPart
import com.ganainy.gymmasterscompose.ui.models.Equipment
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutExercise

// UI States
sealed interface CreateWorkoutAction {
    object NavigateBack : CreateWorkoutAction
    data class NavigateToExercise(val exercise: Exercise) : CreateWorkoutAction
    object ShowFilterSheet : CreateWorkoutAction
    object NavigateToFeed : CreateWorkoutAction
    object UploadWorkout : CreateWorkoutAction
    data class SearchQueryChanged(val query: String) : CreateWorkoutAction
    data class EditWorkout(val workout: Workout) : CreateWorkoutAction
    data class ExerciseSelected(val exercise: Exercise) : CreateWorkoutAction
    data class ExerciseModified(val exercise: WorkoutExercise) : CreateWorkoutAction
    data class ExerciseDeleted(val exercise: WorkoutExercise) : CreateWorkoutAction
    object DismissAddExerciseDialog : CreateWorkoutAction
    object ApplyFilters : CreateWorkoutAction
    object ClearFilters : CreateWorkoutAction
    object HideFilterSheet : CreateWorkoutAction

    data class AddWorkoutExercise(val exercise: WorkoutExercise?) : CreateWorkoutAction
    data class EditWorkoutExercise(val exercise: WorkoutExercise?) : CreateWorkoutAction
    data class BodyPartFilterChange(val bodyPart: BodyPart) : CreateWorkoutAction
    data class EquipmentFilterChange(val equipment: Equipment) : CreateWorkoutAction
    data class TargetMuscleFilterChange(val targetMuscle: TargetMuscle) : CreateWorkoutAction
    object ToggleExerciseWorkoutListShow : CreateWorkoutAction

}