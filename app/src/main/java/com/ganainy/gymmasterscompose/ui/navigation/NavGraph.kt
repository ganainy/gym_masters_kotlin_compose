
import android.net.Uri
import android.util.Log
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ganainy.gymmasterscompose.AuthUiState
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.models.Exercise
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.workout.Workout
import com.ganainy.gymmasterscompose.ui.screens.create_post.CreatePostScreen
import com.ganainy.gymmasterscompose.ui.screens.create_workout.WorkoutSetupScreen
import com.ganainy.gymmasterscompose.ui.screens.discover.DiscoverScreen
import com.ganainy.gymmasterscompose.ui.screens.exercise.ExerciseScreen
import com.ganainy.gymmasterscompose.ui.screens.exercise_list.ExerciseListScreen
import com.ganainy.gymmasterscompose.ui.screens.feed.FeedScreen
import com.ganainy.gymmasterscompose.ui.screens.post_details.PostDetailsScreen
import com.ganainy.gymmasterscompose.ui.screens.profile.ProfileScreen
import com.ganainy.gymmasterscompose.ui.screens.signin.SignInScreen
import com.ganainy.gymmasterscompose.ui.screens.signup.SignUpScreen
import com.ganainy.gymmasterscompose.ui.screens.workout_details.WorkoutDetailsScreen
import com.ganainy.gymmasterscompose.ui.screens.workout_list.WorkoutListScreen
import com.google.gson.Gson
import javax.inject.Inject

// NavigationRoutes.kt
object NavigationRoutes {
    const val AUTH_GRAPH = "auth"
    const val MAIN_GRAPH = "main"

    object Auth {
        const val SIGN_IN = "$AUTH_GRAPH/sign_in"
        const val SIGN_UP = "$AUTH_GRAPH/sign_up"
    }

    object Main {
        const val FEED = "$MAIN_GRAPH/feed"
        const val DISCOVER = "$MAIN_GRAPH/discover"
        const val WORKOUT_LIST = "$MAIN_GRAPH/workout_list"
        const val EXERCISE_LIST = "$MAIN_GRAPH/exercise_list"
        const val PROFILE = "$MAIN_GRAPH/profile"
        const val CREATE_POST = "$MAIN_GRAPH/create_post"
        const val EXERCISE_DETAILS = "$MAIN_GRAPH/exercise_details"
        const val WORKOUT_SETUP = "$MAIN_GRAPH/workout_setup"
        const val WORKOUT_DETAILS = "$MAIN_GRAPH/workout_details"
        const val POST_DETAILS = "$MAIN_GRAPH/post_details"
    }
}

// NavigationArgs.kt
object NavigationArgs {
    const val USER_ID = "userId"
    const val WORKOUT = "workout"
    const val EXERCISE = "exercise"
    const val POST = "post"
    const val IS_LIKED = "isLiked"
    const val IS_SAVED = "isSaved"
}

// Screen.kt
sealed class Screen(
    val route: String,
    val icon: Int? = null,
    val label: String? = null
) {
    sealed class Auth(route: String) : Screen(route) {
        object SignIn : Auth(NavigationRoutes.Auth.SIGN_IN)
        object SignUp : Auth(NavigationRoutes.Auth.SIGN_UP)
    }

    sealed class Main(route: String, icon: Int? = null, label: String? = null) :
        Screen(route, icon, label) {
        object Feed : Main(NavigationRoutes.Main.FEED, R.drawable.home, "Feed")
        object Discover : Main(NavigationRoutes.Main.DISCOVER, R.drawable.search, "Discover")
        object WorkoutList :
            Main(NavigationRoutes.Main.WORKOUT_LIST, R.drawable.dumbbells, "Workouts")

        object ExerciseList :
            Main(NavigationRoutes.Main.EXERCISE_LIST, R.drawable.dumbbell, "Exercises")

        object Profile : Main(NavigationRoutes.Main.PROFILE, R.drawable.profile, "Profile")
    }

    object CreatePost : Screen(NavigationRoutes.Main.CREATE_POST)
    object Exercise : Screen(NavigationRoutes.Main.EXERCISE_DETAILS)
    object WorkoutSetup : Screen(NavigationRoutes.Main.WORKOUT_SETUP)
    object WorkoutDetails : Screen(NavigationRoutes.Main.WORKOUT_DETAILS)
    object PostDetails : Screen(NavigationRoutes.Main.POST_DETAILS)
}

// AppNavigator.kt
class AppNavigator @Inject constructor(private val navController: NavHostController) {


    fun navigate(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    fun navigateToAuth() {
        navController.navigate(NavigationRoutes.AUTH_GRAPH) {
            popUpTo(0) { inclusive = true }
        }
    }

    fun navigateToMain() {
        navController.navigate(NavigationRoutes.MAIN_GRAPH) {
            popUpTo(0) { inclusive = true }
        }
    }

    fun navigateToProfile(userId: String?) {
        val route = "${NavigationRoutes.Main.PROFILE}?${NavigationArgs.USER_ID}=${userId ?: ""}"
        navController.navigate(route)
    }

    fun navigateToWorkoutDetails(workout: Workout, isLiked: Boolean, isSaved: Boolean) {
        val workoutJson = Uri.encode(Gson().toJson(workout))
        val route = "${NavigationRoutes.Main.WORKOUT_DETAILS}?" +
                "${NavigationArgs.WORKOUT}=$workoutJson&" +
                "${NavigationArgs.IS_LIKED}=$isLiked&" +
                "${NavigationArgs.IS_SAVED}=$isSaved"
        navController.navigate(route)
    }

    fun navigateToExerciseDetails(exercise: Exercise) {
        val exerciseJson = Uri.encode(Gson().toJson(exercise))
        val route =
            "${NavigationRoutes.Main.EXERCISE_DETAILS}?${NavigationArgs.EXERCISE}=$exerciseJson"
        navController.navigate(route)
    }

    fun navigateToPostDetails(post: FeedPost, isLiked: Boolean) {
        val postJson = Uri.encode(Gson().toJson(post))
        val route = "${NavigationRoutes.Main.POST_DETAILS}?" +
                "${NavigationArgs.POST}=$postJson&" +
                "${NavigationArgs.IS_LIKED}=$isLiked"
        navController.navigate(route)
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    fun navigateToCreatePost() {
        navController.navigate(Screen.CreatePost.route)
    }

    fun navigateToFeed() {
        navController.navigate(Screen.Main.Feed.route)
    }

}

// AppNavigation.kt
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    navigator: AppNavigator = remember { AppNavigator(navController) }
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = "loading"
    ) {
        // Loading Screen
        composable(route = "loading") {
            LoadingScreen()
        }

        // Auth Graph
        authGraph(navigator)

        // Main Graph
        mainGraph(navigator)
    }
}

// AuthNavigation.kt
private fun NavGraphBuilder.authGraph(navigator: AppNavigator) {
    navigation(
        startDestination = Screen.Auth.SignIn.route,
        route = NavigationRoutes.AUTH_GRAPH
    ) {
        composable(route = Screen.Auth.SignIn.route) {
            SignInScreen(
                onSignInSuccess = navigator::navigateToMain,
                navigateToSignUp = { navigator.navigate(Screen.Auth.SignUp.route) }
            )
        }

        composable(route = Screen.Auth.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = navigator::navigateToMain,
                navigateToSignIn = { navigator.navigate(Screen.Auth.SignIn.route) }
            )
        }
    }
}

// MainNavigation.kt
private fun NavGraphBuilder.mainGraph(navigator: AppNavigator) {
    navigation(
        startDestination = Screen.Main.Feed.route,
        route = NavigationRoutes.MAIN_GRAPH
    ) {
        // Feed Screen
        composable(route = Screen.Main.Feed.route) {
            FeedScreen(
                navigateToProfile = navigator::navigateToProfile,
                navigateToCreatePost = navigator::navigateToCreatePost,
                navigateToDetailedPost = { post, isLiked ->
                    navigator.navigateToPostDetails(post, isLiked)
                }
            )
        }

        // Discover Screen
        composable(route = Screen.Main.Discover.route) {
            DiscoverScreen(
                navigateToProfile = navigator::navigateToProfile
            )
        }

        // Exercise List Screen
        composable(route = Screen.Main.ExerciseList.route) {
            ExerciseListScreen(
                navigateToExercise = navigator::navigateToExerciseDetails,
                navigateBack = navigator::navigateBack
            )
        }

        // Profile Screen
        composable(
            route = "${Screen.Main.Profile.route}?${NavigationArgs.USER_ID}={${NavigationArgs.USER_ID}}",
            arguments = listOf(
                navArgument(NavigationArgs.USER_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(NavigationArgs.USER_ID)
            ProfileScreen(
                userId = userId,
                navigateBack = navigator::navigateBack,
                navigateToLogin = navigator::navigateToAuth,
                navigateToCreatePost = { navigator.navigate(Screen.CreatePost.route) },
                navigateToWorkoutSetup = { navigator.navigate(Screen.WorkoutSetup.route) },
                navigateToWorkoutDetails = { workoutWithStatus ->
                    navigator.navigateToWorkoutDetails(
                        workoutWithStatus.workout,
                        workoutWithStatus.isLiked,
                        workoutWithStatus.isSaved
                    )
                },
                navigateToExercisesList = { navigator.navigate(Screen.Main.ExerciseList.route) },
                navigateToExerciseDetails = navigator::navigateToExerciseDetails
            )
        }

        // Additional screens with similar pattern...
        // Exercise Details Screen
        composable(
            route = "${Screen.Exercise.route}?${NavigationArgs.EXERCISE}={${NavigationArgs.EXERCISE}}",
            arguments = listOf(
                navArgument(NavigationArgs.EXERCISE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val exerciseJson = backStackEntry.arguments?.getString(NavigationArgs.EXERCISE)
            val exercise = Gson().fromJson(exerciseJson, Exercise::class.java)
            ExerciseScreen(
                exercise = exercise,
                navigateBack = navigator::navigateBack
            )
        }

        // Post Details Screen
        composable(
            route = "${Screen.PostDetails.route}?" +
                    "${NavigationArgs.POST}={${NavigationArgs.POST}}",
            arguments = listOf(
                navArgument(NavigationArgs.POST) { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val postJson = backStackEntry.arguments?.getString(NavigationArgs.POST)
            val post = Gson().fromJson(postJson, FeedPost::class.java)
            PostDetailsScreen(
                navigateBack = navigator::navigateBack,
                post = post,
            )
        }

        // ExerciseList Screen
        composable(
            route = Screen.Main.ExerciseList.route,
        ) { backStackEntry ->
            ExerciseListScreen(
                navigateToExercise = navigator::navigateToExerciseDetails,
                navigateBack = navigator::navigateBack
            )
        }

        //Workout list screen
        composable(
            route = Screen.Main.WorkoutList.route,
        ) { backStackEntry ->
            WorkoutListScreen(
                navigateToWorkoutDetails = navigator::navigateToWorkoutDetails,
            )

    }



        //Workout Details Screen
        composable(
            route = "${Screen.WorkoutDetails.route}?" +
                    "${NavigationArgs.WORKOUT}={${NavigationArgs.WORKOUT}}&" +
                    "${NavigationArgs.IS_LIKED}={${NavigationArgs.IS_LIKED}}&" +
                    "${NavigationArgs.IS_SAVED}={${NavigationArgs.IS_SAVED}}",
            arguments = listOf(
                navArgument(NavigationArgs.WORKOUT) { type = NavType.StringType },
                navArgument(NavigationArgs.IS_LIKED) { type = NavType.BoolType },
                navArgument(NavigationArgs.IS_SAVED) { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val workoutJson = backStackEntry.arguments?.getString(NavigationArgs.WORKOUT)
            val isLiked = backStackEntry.arguments?.getBoolean(NavigationArgs.IS_LIKED) ?: false
            val isSaved = backStackEntry.arguments?.getBoolean(NavigationArgs.IS_SAVED) ?: false
            val workout = Gson().fromJson(workoutJson, Workout::class.java)
            WorkoutDetailsScreen(
                workout = workout,
                isLiked = isLiked,
                isSaved = isSaved,
                navigateBack = navigator::navigateBack,
                navigateToExerciseDetails = navigator::navigateToExerciseDetails
            )
        }

        // Workout Setup Screen
        composable(route = Screen.WorkoutSetup.route) { backStackEntry ->
            WorkoutSetupScreen(
                navigateToFeed = navigator::navigateToFeed,
                navigateBack = navigator::navigateBack,
                navigateToExercise = navigator::navigateToExerciseDetails
            )
        }

        //Create post screen
        composable(route = Screen.CreatePost.route) {
            CreatePostScreen(
                onNavigateBack = navigator::navigateBack,
            )
        }

    }
    }

// MainScreen.kt
@Composable
fun MainScreen(authState: AuthUiState) {
    val navController = rememberNavController()
    val navigator = remember { AppNavigator(navController) }

    // Use LaunchedEffect with currentDestination to delay navigation
    LaunchedEffect(authState, navController.currentDestination) {
        if (navController.currentDestination != null) {
            when (authState) {
                is AuthUiState.Authenticated -> {
                    navigator.navigateToMain()
                }
                is AuthUiState.Unauthenticated -> {
                    navigator.navigateToAuth()
                }
                is AuthUiState.Loading -> {
                    // Show loading screen
                }
            }
        } else {
            Log.e("MainScreen", "Navigation graph not ready, skipping navigation")
        }
    }

    // Render AppNavigation based on authState
    when (authState) {
        is AuthUiState.Loading -> {
            LoadingScreen()
        }
        else -> {
            AppNavigation(navController = navController, navigator = navigator)
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
        Box(modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding())) {
            // App navigation setup
            AppNavigation(
                navController = navController
            )
        }
    }

}


// BottomNavBar.kt
@Composable
fun BottomNavBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    if (currentRoute?.startsWith(NavigationRoutes.MAIN_GRAPH) == true) {
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
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
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