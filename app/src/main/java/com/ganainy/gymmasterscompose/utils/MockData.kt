package com.ganainy.gymmasterscompose.utils

import Comment
import UserDisplayInfo
import com.ganainy.gymmasterscompose.ui.models.BodyPart
import com.ganainy.gymmasterscompose.ui.models.Equipment
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.TargetMuscle
import com.ganainy.gymmasterscompose.ui.models.User
import com.ganainy.gymmasterscompose.ui.models.UserStats
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.models.post.PostMetrics
import com.ganainy.gymmasterscompose.ui.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutExercise
import com.ganainy.gymmasterscompose.ui.models.workout.WorkoutMetrics
import com.ganainy.gymmasterscompose.ui.screens.post_details.CommentWithLikeStatus
import com.ganainy.gymmasterscompose.ui.screens.post_details.FeedPostWithLikesAndComments
import com.ganainy.gymmasterscompose.ui.screens.workout_list.WorkoutWithStatus
import com.google.firebase.Timestamp

//sample data to be used in preview functions and testing
object MockData {


    // Sample Exercise data
    val sampleExercise = Exercise(
        id = "1",
        name = "Push Up",
        bodyPart = "chest",
        equipment = "equipment-name",
        target = "upper-body",
        secondaryMuscles = listOf("Triceps", "Shoulders"),
        instructions = listOf(
            "Step 1: Start in a plank position with your hands shoulder-width apart.",
            "Step 2: Lower your body until your chest touches the floor.",
            "Step 3: Push your body back up to the starting position.",
            "Step 4: Repeat for desired repetitions."
        ),
        screenshotPath = "https://www.example.com/images/pushup.jpg",
        isSavedLocally = false
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

    private val sampleWorkoutExercise2 = WorkoutExercise(
        exercise = sampleExercise2,
        order = 2,
        sets = 4,
        reps = 12,
        restBetweenSets = 50
    )

    private val sampleWorkoutExercise3 = WorkoutExercise(
        exercise = sampleExercise3,
        order = 3,
        sets = 3,
        reps = 10,
        restBetweenSets = 30
    )

    private val sampleWorkoutExercise4 = WorkoutExercise(
        exercise = sampleExercise4,
        sets = 3,
        reps = 8,
        order = 1,
        restBetweenSets = 60,
    )
    private val sampleWorkoutExercise5 = WorkoutExercise(
        exercise = sampleExercise5,
        sets = 4,
        reps = 10,
        order = 2,
        restBetweenSets = 30,
    )

    val sampleWorkoutExerciseList = listOf(
        sampleWorkoutExercise,
        sampleWorkoutExercise2,
        sampleWorkoutExercise3,
        sampleWorkoutExercise4,
        sampleWorkoutExercise5
    )

    // Sample Workout data
    val sampleWorkout = Workout(
        id = "1234567890",
        creatorId = "creator123",
        title = "Sample Workout",
        description = "This is a sample workout",
        difficulty = "Intermediate",
        workoutDuration = "45",
dateCreated = Timestamp(Timestamp.now().seconds - (60L * 60 * 24 * 7), 0), // 1 week ago
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
        joinDate = Timestamp.now(),
        profilePictureUrl = "https://example.com/profile_picture.jpg",
        bio = "I'm a fitness enthusiast",
        lastActive = Timestamp.now(),
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
        joinDate = Timestamp.now(),
        profilePictureUrl = "https://pbs.twimg.com/profile_images/1240119990411550720/hBEe3tdn_bigger.jpg",
        bio = "I'm a fitness enthusiast and a software engineer",
        lastActive = Timestamp.now(),
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
        joinDate = Timestamp.now(),
        profilePictureUrl = "https://pbs.twimg.com/profile_images/1240119990411550720/hBEe3tdn_bigger.jpg",
        bio = "I'm a fitness enthusiast and a personal trainer",
        lastActive = Timestamp.now(),
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
        createdAt = Timestamp.now(),
        tags = listOf("workout", "motivation"),
        postMetrics = PostMetrics(likes = 20, comments = 3)
    )

    val samplePost2 = FeedPost(
        id = "2",
        content = "Healthy eating is key to #success.",
        postCreator = PostCreator(
            id = "user2",
            displayName = "Bob Johnson",
            profilePictureUrl = "https://randomuser.me/api/portraits/men/2.jpg",
        ),
        createdAt = Timestamp.now(),
        tags = listOf("nutrition", "health"),
        postMetrics = PostMetrics(likes = 15, comments = 4),
        imageUrlList = listOf(
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg",
            "https://example.com/image3.jpg"
        )
    )

    val samplePost3 = FeedPost(
        id = "3",
        content = "Morning run with a beautiful sunrise.",
        postCreator = PostCreator(
            id = "user3",
            displayName = "Charlie Brown",
            profilePictureUrl = "https://randomuser.me/api/portraits/men/3.jpg",
        ),
        createdAt = Timestamp.now(),
        tags = listOf("running", "morning"),
        postMetrics = PostMetrics(likes = 30, comments = 8),
        imageUrlList = listOf("https://example.com/image1.jpg", "https://example.com/image2.jpg")
    )

    val samplePostList = listOf(samplePost, samplePost2, samplePost3)

    //sample comments
    val sampleComment = Comment(
        id = "1",
        content = "Great job!",
        userDisplayInfo = UserDisplayInfo(
            displayName = "Eve Johnson",
            profileImageUrl = "https://randomuser.me/api/portraits/women/4.jpg",
        ),
    )

    val sampleComment2 = Comment(
        id = "2",
        content = "Thanks for sharing!",
        userDisplayInfo = UserDisplayInfo(
            displayName = "Frank Wilson",
            profileImageUrl = "https://randomuser.me/api/portraits/men/5.jpg",
        ),
    )

    val sampleCommentList = listOf(sampleComment, sampleComment2)
    val sampleCommentWithLikeStatusList = listOf(
        CommentWithLikeStatus(
            comment = sampleComment,
            isLiked = true
        ),
        CommentWithLikeStatus(
            comment = sampleComment2,
            isLiked = false
        ),
    )


    val samplePostWithLikesAndComments = FeedPostWithLikesAndComments(
        post = samplePost,
        isLiked = true,
        commentList = sampleCommentWithLikeStatusList
    )

    val samplePostWithLikesAndComments2 = FeedPostWithLikesAndComments(
        post = samplePost2,
        isLiked = false,
        commentList = sampleCommentWithLikeStatusList
    )

    val samplePostWithLikesAndComments3 = FeedPostWithLikesAndComments(
        post = samplePost3,
        isLiked = true,
        commentList = sampleCommentWithLikeStatusList
    )


    val samplePostWithLikesAndCommentsList = listOf(
        samplePostWithLikesAndComments,
        samplePostWithLikesAndComments2,
        samplePostWithLikesAndComments3
    )

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
