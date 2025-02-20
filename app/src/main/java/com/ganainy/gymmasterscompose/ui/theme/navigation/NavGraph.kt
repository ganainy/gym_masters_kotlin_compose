package com.ganainy.gymmasterscompose.ui.theme.navigation


import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.ganainy.gymmasterscompose.AuthUiState
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.Exercise
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.theme.screens.create_post.CreatePostScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.create_workout.CreateWorkoutViewModel
import com.ganainy.gymmasterscompose.ui.theme.screens.create_workout.WorkoutExerciseListScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.create_workout.WorkoutSetupScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.discover.DiscoverScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.exercise.ExerciseScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.exercise_list.ExerciseListScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.feed.FeedScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.post_details.PostDetailsScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.profile.ProfileScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.signin.SignInScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.signup.SignUpScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_details.WorkoutDetailsScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.workout_list.WorkoutListScreen
import com.google.gson.Gson


sealed class Screen(val route: String, val icon: Int? = null, val label: String? = null) {
    // Auth Screens
    sealed class Auth(route: String) : Screen(route) {
        object SignUp : Auth("auth/sign_up")
        object SignIn : Auth("auth/sign_in")
    }

    // Main App Screens (Bottom Nav)
    sealed class Main(route: String, icon: Int? = null, label: String? = null) :
        Screen(route, icon, label) {
        object Feed : Main("main/feed", R.drawable.home, "Feed")
        object Discover : Main("main/discover", R.drawable.search, "Discover")
        object WorkoutList : Main("main/workout_list", R.drawable.dumbbells, "Workouts")
        object ExerciseList : Main("main/exercise_list", R.drawable.dumbbell, "Exercises")
        object Profile : Main("main/profile?userId={userId}", R.drawable.profile, "Profile") {
            fun createRoute(userId: String? = null) =
                "main/profile" + (userId?.let { "?userId=$it" } ?: "")
        }
    }

    // Other Screens (Not in Bottom Nav)
    object CreatePost : Screen("create_post")
    object Exercise : Screen("exercise")
    object WorkoutSetup : Screen("workout_setup")
    object WorkoutExerciseList : Screen("workout_exercise_list")
    object DetailedPost : Screen("detailed_post")
    object DetailedWorkout : Screen("detailed_workout")
}


/*
* Auth Graph and Main Graph are separated with different navigation() subgraphs to avoid back
* navigation from main to auth
* */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startRoute: String = "loading"
) {
    // NavHost sets up the navigation graph
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = "loading"
    ) {
        // Loading destination
        composable(route = "loading") {
            LoadingScreen()
        }
        // Auth Graph
        navigation(
            startDestination = Screen.Auth.SignIn.route,
            route = "auth"
        ) {
            // SignInScreen composable
            composable(route = Screen.Auth.SignIn.route) {
                SignInScreen(
                    onSignInSuccess = {
                        // Navigate to main graph on sign-in success
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    },
                    navigateToSignUp = {
                        // Navigate to SignUpScreen
                        navController.navigate(Screen.Auth.SignUp.route)
                    }
                )
            }
            // SignUpScreen composable
            composable(route = Screen.Auth.SignUp.route) {
                SignUpScreen(
                    onSignUpSuccess = {
                        // Navigate to main graph on sign-up success
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    },
                    navigateToSignIn = {
                        // Navigate to SignInScreen
                        navController.navigate(Screen.Auth.SignIn.route)
                    }
                )
            }
        }

        // Main Graph
        navigation(
            startDestination = Screen.Main.Feed.route,
            route = "main"
        ) {
            // FeedScreen composable
            composable(route = Screen.Main.Feed.route) {
                FeedScreen(navController,

                    navigateToDetailedPost = { post,isLiked ->
                        // Encode post object to JSON and navigate to DetailedPost
                        val postJson = Uri.encode(Gson().toJson(post))
                        navController.navigate("${Screen.DetailedPost.route}?post=$postJson?isLiked=$isLiked")
                    }

                )


            }
            // DiscoverScreen composable
            composable(route = Screen.Main.Discover.route) { DiscoverScreen(navController) }
            // WorkoutListScreen composable
            composable(route = Screen.Main.WorkoutList.route) { WorkoutListScreen(navigateToWorkoutDetails = { workout,isLiked,isSaved ->
                    // Encode exercise object to JSON and navigate to ExerciseScreen
                    val workoutJson = Uri.encode(Gson().toJson(workout))
                    navController.navigate("${Screen.DetailedWorkout.route}?workout=$workoutJson?isLiked=$isLiked?isSaved=$isSaved")
                }

            ) }
            // ExerciseListScreen composable
            composable(route = Screen.Main.ExerciseList.route) {
                ExerciseListScreen(
                    navigateToExercise = { exercise ->
                        // Encode exercise object to JSON and navigate to ExerciseScreen
                        val exerciseJson = Uri.encode(Gson().toJson(exercise))
                        navController.navigate("${Screen.Exercise.route}?exercise=$exerciseJson")
                    },
                    navigateBack = { navController.popBackStack() }
                )
            }
            // ProfileScreen composable
            composable(
                route = Screen.Main.Profile.route,
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
                        // Navigate to auth graph
                        navController.navigate("auth") {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    navigateToCreatePost = {
                        // Navigate to CreatePostScreen
                        navController.navigate(Screen.CreatePost.route)
                    },
                    navigateToWorkoutSetup = {
                        // Navigate to WorkoutSetupScreen
                        navController.navigate(Screen.WorkoutSetup.route)

                    },
                    navigateToWorkoutDetails = { workoutWithStatus ->
                        // Encode workout object to JSON and navigate to Detailed Workout Screen
                        val workoutJson = Uri.encode(Gson().toJson(workoutWithStatus.workout))
                        navController.navigate("${Screen.DetailedWorkout.route}?workout=$workoutJson?isLiked=${workoutWithStatus.isLiked}?isSaved=${workoutWithStatus.isSaved}")
                    },
                    navigateToExercisesList = {
                        // Navigate to ExerciseListScreen
                        navController.navigate(Screen.Main.ExerciseList.route)
                    },
                    navigateToExerciseDetails = { exercise ->
                        // Encode exercise object to JSON and navigate to ExerciseScreen
                        val exerciseJson = Uri.encode(Gson().toJson(exercise))
                        navController.navigate("${Screen.Exercise.route}?exercise=$exerciseJson")
                    },
                )
            }

            // CreatePostScreen composable
            composable(route = Screen.CreatePost.route) {
                CreatePostScreen(onNavigateBack = { navController.popBackStack() })
            }

            // ExerciseScreen composable
            composable(
                "${Screen.Exercise.route}?exercise={exercise}",
                arguments = listOf(navArgument("exercise") { type = NavType.StringType })
            ) { backStackEntry ->
                val exerciseJson = backStackEntry.arguments?.getString("exercise")
                val exercise = Gson().fromJson(exerciseJson, Exercise::class.java)
                ExerciseScreen(
                    exercise = exercise,
                    navigateBack = { navController.popBackStack() }
                )
            }

            // WorkoutSetupScreen composable
            composable(route = Screen.WorkoutSetup.route) {
                val viewModel = hiltViewModel<CreateWorkoutViewModel>()
                WorkoutSetupScreen(
                    navigateToCreateWorkoutExerciseList = {
                        // Navigate to WorkoutExerciseListScreen
                        navController.navigate(Screen.WorkoutExerciseList.route)
                    },
                    navigateToFeed = {
                        // Navigate to FeedScreen
                        navController.navigate(Screen.Main.Feed.route) {
                            popUpTo(Screen.Main.Feed.route) { inclusive = true }
                        }
                    },
                    viewModel = viewModel
                )
            }

            // WorkoutExerciseListScreen composable
            composable(route = Screen.WorkoutExerciseList.route) {
                val viewModel = hiltViewModel<CreateWorkoutViewModel>()
                WorkoutExerciseListScreen(
                    navigateBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }

            // DetailedPostScreen composable
            composable(
                "${Screen.DetailedPost.route}?post={post}?isLiked={isLiked}",
                arguments = listOf(
                    navArgument("post") { type = NavType.StringType },
                    navArgument("isLiked") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val postJson = backStackEntry.arguments?.getString("post")
                val isLiked = backStackEntry.arguments?.getBoolean("isLiked")
                val post = Gson().fromJson(postJson, FeedPost::class.java)
                PostDetailsScreen(
                    post = post,
                    isLiked = isLiked ?: false,
                )
            }

            // DetailedWorkoutScreen composable
            composable(
                "${Screen.DetailedWorkout.route}?workout={workout}?isLiked={isLiked}?isSaved={isSaved}",
                arguments = listOf(
                    navArgument("workout") { type = NavType.StringType },
                    navArgument("isLiked") { type = NavType.BoolType },
                    navArgument("isSaved") { type = NavType.BoolType },
                )
            ) { backStackEntry ->
                val workoutJson = backStackEntry.arguments?.getString("workout")
                val isLiked = backStackEntry.arguments?.getBoolean("isLiked")
                val isSaved = backStackEntry.arguments?.getBoolean("isSaved")
                val workout = Gson().fromJson(workoutJson, Workout::class.java)
                WorkoutDetailsScreen(
                    workout = workout,
                    isLiked = isLiked ?: false,
                    isSaved = isSaved ?: false,
                    navigateToExerciseDetails = { exercise ->
                        // Encode exercise object to JSON and navigate to ExerciseScreen
                        val exerciseJson = Uri.encode(Gson().toJson(exercise))
                        navController.navigate("${Screen.Exercise.route}?exercise=$exerciseJson")
                    }
                )
            }



        }
    }
}

/**
 * Composable function for the main screen of the app.
 *
 * @param authState The current authentication state of the user.
 */
@Composable
fun MainScreen(
    authState: AuthUiState
) {
    // Remember the NavController instance
    val navController = rememberNavController()

    // Handle initial destination based on auth state
    LaunchedEffect(authState) {
        when (authState) {
            AuthUiState.Authenticated -> {
                navController.navigate("main") {
                    popUpTo(0) { inclusive = true }
                }
            }

            AuthUiState.Unauthenticated -> {
                navController.navigate("auth") {
                    popUpTo(0) { inclusive = true }
                }
            }

            AuthUiState.Loading -> {} // Do nothing, we're already at loading screen
        }
    }

    // Scaffold layout with a bottom bar
    Scaffold(
        bottomBar = {
            // Only show bottom bar when in main graph
            val currentRoute =
                navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute?.startsWith("main/") == true) {
                BottomNavBar(navController)
            }
        }
    ) { paddingValues ->
        // Box layout to handle padding values
        Box(modifier = Modifier.padding(paddingValues)) {
            // App navigation setup
            AppNavigation(
                modifier = Modifier.fillMaxSize(),
                navController = navController
            )
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Only show bottom nav for main screens
    if (currentRoute?.startsWith("main/") == true) {
        NavigationBar {
            val items = listOf(
                Screen.Main.Feed,
                Screen.Main.Discover,
                Screen.Main.WorkoutList,
                Screen.Main.ExerciseList,
                Screen.Main.Profile
            )

            items.forEach { screen ->
                NavigationBarItem(
                    icon = {
                        screen.icon?.let {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(it),
                                contentDescription = null
                            )
                        }
                    },
                    label = { Text(screen.label ?: "") },
                    selected = currentRoute == screen.route,
                    onClick = {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
