package com.ganainy.gymmasterscompose.utils

import com.ganainy.gymmasterscompose.ui.theme.models.BodyPart
import com.ganainy.gymmasterscompose.ui.theme.models.Equipment
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutMetrics

//sample data to be used in preview functions and testing
object MockData {


    // Sample Exercise data
    val sampleExercise = Exercise(
        id = "1",
        name = "Push Up",
        bodyPart = "Chest",
        equipment = "Bodyweight",
        target = "Pectoralis Major",
        screenshotPath = "https://example.com/pushup.jpg" // Replace with actual image URL or local resource
    )

    val sampleExercise2 = Exercise(
        id = "2",
        name = "Bicep Curl",
        bodyPart = "Biceps",
        equipment = "Dumbbells",
        target = "Biceps Brachii",
        screenshotPath = "https://example.com/bicepcurl.jpg" // Replace with actual image URL or local resource
    )

    val sampleExercise3 = Exercise(
        id = "3",
        name = "Tricep Dip",
        bodyPart = "Triceps",
        equipment = "Bodyweight",
        target = "Triceps Brachii",
        screenshotPath = "https://example.com/tricepdip.jpg" // Replace with actual image URL or local resource
    )

    val sampleExerciseList = listOf(sampleExercise, sampleExercise2, sampleExercise3)

    // Sample WorkoutExercise data
    val sampleWorkoutExercise = WorkoutExercise(
        exercise = sampleExercise,
        order = 1,
        sets = 3,
        reps = 12,
        restBetweenSets = 60
    )

    val sampleWorkoutExercise2 = WorkoutExercise(
        exercise = sampleExercise2,
        order = 2,
        sets = 4,
        reps = 12,
        restBetweenSets = 50
    )

    val sampleWorkoutExercise3 = WorkoutExercise(
        exercise = sampleExercise3,
        order = 3,
        sets = 3,
        reps = 10,
        restBetweenSets = 30
    )

    // Sample Workout data
    val sampleWorkout = Workout(
        id = "1234567890",
        creatorId = "creator123",
        title = "Sample Workout",
        description = "This is a sample workout",
        difficulty = "Intermediate",
        workoutDuration = "45",
        dateCreated = 1643723400,
        imageUrl = "https://example.com/workout_image.jpg",
        imagePath = "",
        tags = listOf("strength", "cardio"),
        isPublic = true,
        workoutExerciseList = listOf(sampleWorkoutExercise,sampleWorkoutExercise2,sampleWorkoutExercise3),
        workoutMetrics = WorkoutMetrics(likesCount = 10, saveCount = 5)
    )


    val sampleBodyPartList = listOf(BodyPart("Chest"), BodyPart("Back"))
    val sampleEquipmentList = listOf(Equipment("Dumbbell"), Equipment("Barbell"))
    val sampleTargetList = listOf(TargetMuscle("Biceps"), TargetMuscle("Triceps"))
}
