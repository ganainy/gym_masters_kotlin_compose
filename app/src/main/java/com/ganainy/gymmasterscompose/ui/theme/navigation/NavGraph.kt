package com.ganainy.gymmasterscompose.ui.theme.navigation


import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.screens.create_post.CreatePostScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.create_workout.CreateWorkoutViewModel
import com.ganainy.gymmasterscompose.ui.theme.screens.create_workout.WorkoutExerciseListScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.create_workout.WorkoutSetupScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.discover.DiscoverScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.exercise.ExerciseScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.exercise_list.ExerciseListScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.feed.FeedScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.profile.ProfileScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.signin.SignInScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.signup.SignUpScreen
import com.google.gson.Gson


sealed class Screen(val route: String) {


    object SignUp : Screen("Sign_up")
    object SignIn : Screen("sign_in")
    object Feed : Screen("feed")
    object Discover : Screen("discover")
    object CreatePost : Screen("create_post")
    object Profile : Screen("profile?userId={userId}") {
        // Helper function to create route with optional userId
        fun createRoute(userId: String? = null) =
            "profile" + (userId?.let { "?userId=$it" } ?: "")
    }

    object ExerciseList : Screen("exercise_list")
    object Exercise : Screen("exercise")
    object WorkoutSetup : Screen("workout_setup")
    object WorkoutExerciseList : Screen("workout_exercise_list")

}

@Composable
fun AppNavGraph(navController: NavHostController) {

    //shared view model between WorkoutExerciseList and WorkoutSetup screens
    val sharedViewModel = hiltViewModel<CreateWorkoutViewModel>()

    val actions = remember(navController) { NavigationActions(navController) }
    NavHost(navController = navController, startDestination = Screen.SignUp.route) {
        composable(route = Screen.SignUp.route) {
            SignUpScreen(
                actions.navigateToSignIn,
                actions.navigateToFeed,
                actions.navigateBack
            )
        }
        composable(route = Screen.SignIn.route) {
            SignInScreen(
                actions.navigateToSignUp,
                actions.navigateToFeed,
                actions.navigateBack
            )
        }
        composable(route = Screen.Feed.route) {

            FeedScreen(
                navigateToLogin = actions.navigateToSignIn,
                navigateToCreatePost = actions.navigateToCreatePost,
                navigateToDiscover = actions.navigateToDiscover,
                navigateToProfile = actions.navigateToProfile,
                navigateToWorkout = actions.navigateToWorkout,
                navigateToExercises = actions.navigateToExercises,
                navigateToCreateWorkout = actions.navigateToWorkoutSetup
            )


        }
        composable(route = Screen.Discover.route) {
            DiscoverScreen(navigateToProfile = actions.navigateToProfile)
        }

        composable(route = Screen.CreatePost.route) {
            CreatePostScreen(actions.navigateBack)
        }

        // Profile Screen Navigation
        composable(
            route = Screen.Profile.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(
                userId = userId,
                navigateToLogin = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                },
                navigateToCreatePost = actions.navigateToCreatePost
            )
        }
        composable(route = Screen.ExerciseList.route) {
            ExerciseListScreen(actions.navigateToExercise, actions.navigateBack)
        }

        composable(route = Screen.WorkoutExerciseList.route) {
            WorkoutExerciseListScreen(
                navigateBack = actions.navigateBack,
                viewModel = sharedViewModel
            )
        }

        composable(
            "${Screen.Exercise.route}?exercise={exercise}",
            arguments = listOf(navArgument("exercise") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseJson = backStackEntry.arguments?.getString("exercise")
            val exercise = Gson().fromJson(exerciseJson, Exercise::class.java)
            ExerciseScreen(exercise = exercise, navigateBack = actions.navigateBack)
        }

        composable(route = Screen.WorkoutSetup.route) {
            WorkoutSetupScreen(
                navigateToCreateWorkoutExerciseList = actions.navigateToCreateWorkoutExerciseList,
                navigateToFeed = actions.navigateToFeed,
                viewModel = sharedViewModel
            )
        }

    }
}

class NavigationActions(private val navController: NavHostController) {

    val navigateToWorkout: (String) -> Unit = { workoutId ->
        TODO("Navigate to workout screen with workoutId $workoutId")
    }

    val navigateToProfile: (String?) -> Unit = { userId ->
        navController.navigate(Screen.Profile.createRoute(userId = userId))
    }

    val navigateToExercise: (Exercise) -> Unit = { exercise ->
        val exerciseJson = Uri.encode(Gson().toJson(exercise))
        navController.navigate("${Screen.Exercise.route}?exercise=$exerciseJson")
    }

    val navigateToSignUp: () -> Unit = {
        navController.navigate(Screen.SignUp.route) {
            popUpTo(Screen.SignUp.route) { inclusive = true }
        }
    }
    val navigateToSignIn: () -> Unit = {
        navController.navigate(Screen.SignIn.route)
    }
    val navigateToFeed: () -> Unit = {
        navController.navigate(Screen.Feed.route)
    }
    val navigateToDiscover: () -> Unit = {
        navController.navigate(Screen.Discover.route)
    }

    val navigateToCreatePost: () -> Unit = {
        navController.navigate(Screen.CreatePost.route)
    }

    val navigateToExercises: () -> Unit = {
        navController.navigate(Screen.ExerciseList.route)
    }

    val navigateToWorkoutSetup: () -> Unit = {
        navController.navigate(Screen.WorkoutSetup.route)
    }

    val navigateToCreateWorkoutExerciseList: () -> Unit = {
        navController.navigate(Screen.WorkoutExerciseList.route)
    }

    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }

}
