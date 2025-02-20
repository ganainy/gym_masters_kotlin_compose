package com.ganainy.gymmasterscompose.ui.theme.screens.workout_details


import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ganainy.gymmasterscompose.ui.theme.components.WorkoutCard
import com.ganainy.gymmasterscompose.ui.theme.components.WorkoutViewType
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.WorkoutWithStatus

@Composable
fun WorkoutDetailsContent(
    workoutWithStatus: WorkoutWithStatus?,
    onWorkoutLike: (Workout) -> Unit = {},
    onWorkoutSave: (Workout) -> Unit = {},
    navigateToExerciseDetails: (Exercise) -> Unit = {},
) {
    if (workoutWithStatus == null) {
        return
    }



        WorkoutCard(
            workoutWithStatus = workoutWithStatus,
            onWorkoutClick =  {_, _, _ -> },
            onWorkoutLike = { onWorkoutLike(workoutWithStatus.workout) },
            onWorkoutSave = { onWorkoutSave(workoutWithStatus.workout) },
            onExerciseClick = navigateToExerciseDetails,
            workoutViewType = WorkoutViewType.DETAILED,
        )


}

@Preview
@Composable
fun WorkoutDetailsContentPreview() {
    WorkoutDetailsContent(
        workoutWithStatus = WorkoutWithStatus(
            workout = Workout(
                id = "1",
                title = "Workout Title",
                description = "Workout Description",
                imageUrl = "",
                workoutDuration = "30",
                difficulty = "Beginner",
                workoutExerciseList = listOf(
                    WorkoutExercise(
                        exercise = Exercise(
                            id = "1",
                            name = "Exercise 1",
                            bodyPart = "Chest",
                            equipment = "Dumbbells",
                            target = "Chest",
                        ),
                        sets = 3,
                        reps = 8,
                    ),
                    WorkoutExercise(
                        exercise = Exercise(
                            id = "2",
                            name = "Exercise 2",
                            bodyPart = "Back",
                            equipment = "Pull up bar",
                            target = "Back",
                        ),
                        sets = 3,
                        reps = 12,
                    )
                )
            ),
            isLiked = false,
            isSaved = false
        )
    )
}