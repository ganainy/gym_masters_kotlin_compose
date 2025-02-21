package com.ganainy.gymmasterscompose.utils

import com.ganainy.gymmasterscompose.ui.theme.models.BodyPart
import com.ganainy.gymmasterscompose.ui.theme.models.Equipment
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.models.UserStats
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostMetrics
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.ui.theme.models.workout.WorkoutMetrics
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.WorkoutWithStatus

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

    val sampleExercise4 = Exercise(
        id = "4",
        name = "Pull Up",
        bodyPart = "Back",
        equipment = "Pull Up Bar",
        target = "Latissimus Dorsi",
        screenshotPath = "https://example.com/pullup.jpg" // Replace with actual image URL or local resource
    )

    val sampleExercise5 = Exercise(
        id = "5",
        name = "Squat",
        bodyPart = "Legs",
        equipment = "Bodyweight",
        target = "Quadriceps",
        screenshotPath = "https://example.com/squat.jpg" // Replace with actual image URL or local resource
    )

    val sampleExercise6 = Exercise(
        id = "6",
        name = "Lunges",
        bodyPart = "Legs",
        equipment = "Bodyweight",
        target = "Hamstrings",
        screenshotPath = "https://example.com/lunges.jpg" // Replace with actual image URL or local resource
    )

    val sampleExerciseListSmall = listOf(sampleExercise, sampleExercise2, sampleExercise3)

    val sampleExerciseListLarge = listOf(
        sampleExercise,
        sampleExercise2,
        sampleExercise3,
        sampleExercise4,
        sampleExercise5,
        sampleExercise6
    )

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

    val sampleWorkoutExerciseList = listOf(
        sampleWorkoutExercise,
        sampleWorkoutExercise2,
        sampleWorkoutExercise3
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
        workoutExerciseList = listOf(
            sampleWorkoutExercise,
            sampleWorkoutExercise2,
            sampleWorkoutExercise3
        ),
        workoutMetrics = WorkoutMetrics(likesCount = 10, saveCount = 5)
    )

    //mock users
    val sampleUser = User(
        id = "",
        displayName = "John Doe",
        username = "johndoe",
        email = "BZG9a@example.com",
        joinDate = 1643723400,
        profilePictureUrl = "https://example.com/profile_picture.jpg",
        bio = "I'm a fitness enthusiast",
        lastActive = 1643723400,
        stats = UserStats(
            postCount = 100,
            workoutCount = 200,
            followersCount = 50,
            followingCount = 30
        )
    )

    val sampleUser2 = User(
        id = "",
        displayName = "Jane Doe",
        username = "janedoe",
        email = "janedoe@example.com",
        joinDate = 1643723400,
        profilePictureUrl = "https://pbs.twimg.com/profile_images/1240119990411550720/hBEe3tdn_bigger.jpg",
        bio = "I'm a fitness enthusiast and a software engineer",
        lastActive = 1643723400,
        stats = UserStats(
            postCount = 50,
            workoutCount = 100,
            followersCount = 20,
            followingCount = 10
        )
    )

    val sampleUser3 = User(
        id = "",
        displayName = "John Smith",
        username = "johnsmith",
        email = "johnsmith@example.com",
        joinDate = 1643723400,
        profilePictureUrl = "https://pbs.twimg.com/profile_images/1240119990411550720/hBEe3tdn_bigger.jpg",
        bio = "I'm a fitness enthusiast and a personal trainer",
        lastActive = 1643723400,
        stats = UserStats(
            postCount = 150,
            workoutCount = 250,
            followersCount = 60,
            followingCount = 40
        )
    )

    val sampleUserList = listOf(sampleUser, sampleUser2, sampleUser3)

    // mock posts
    val samplePost = FeedPost(
        id = "1",
        content = "Just finished a great workout!",
        postCreator = PostCreator(
            id = "user1",
            displayName = "Alice Smith",
            profilePictureUrl = "https://randomuser.me/api/portraits/women/1.jpg",
        ),
        createdAt = System.currentTimeMillis(),
        tags = listOf("workout", "motivation"),
        postMetrics = PostMetrics(likes = 20, comments = 3)
    )

    val samplePost2 = FeedPost(
        id = "2",
        content = "Healthy eating is key to success.",
        postCreator = PostCreator(
            id = "user2",
            displayName = "Bob Johnson",
            profilePictureUrl = "https://randomuser.me/api/portraits/men/2.jpg",
        ),
        createdAt = System.currentTimeMillis(),
        tags = listOf("nutrition", "health"),
        postMetrics = PostMetrics(likes = 15, comments = 4)
    )

    val samplePost3 = FeedPost(
        id = "3",
        content = "Morning run with a beautiful sunrise.",
        postCreator = PostCreator(
            id = "user3",
            displayName = "Charlie Brown",
            profilePictureUrl = "https://randomuser.me/api/portraits/men/3.jpg",
        ),
        createdAt = System.currentTimeMillis(),
        tags = listOf("running", "morning"),
        postMetrics = PostMetrics(likes = 30, comments = 8)
    )

    val samplePostList = listOf(samplePost, samplePost2, samplePost3)

    // Sample WorkoutWithStatus data
    val sampleWorkoutWithStatus = WorkoutWithStatus(
        workout = sampleWorkout,
        isLiked = true,
        isSaved = false
    )

    val sampleBodyPartList = listOf(BodyPart("Chest"), BodyPart("Back"))
    val sampleEquipmentList = listOf(Equipment("Dumbbell"), Equipment("Barbell"))
    val sampleTargetList = listOf(TargetMuscle("Biceps"), TargetMuscle("Triceps"))
}
