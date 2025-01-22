package com.ganainy.gymmasterscompose.ui.theme.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ganainy.gymmasterscompose.ui.theme.screens.create_post.CreatePostScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.discover.DiscoverScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.feed.FeedScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.profile.ProfileScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.signin.SignInScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.signup.SignUpScreen


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

}

@Composable
fun AppNavGraph(navController: NavHostController, ) {
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
                navigateToExercise = actions.navigateToExercise,
                navigateToWorkout = actions.navigateToWorkout,
            )


        }
        composable(route = Screen.Discover.route) {
            DiscoverScreen()
        }

        composable(route = Screen.CreatePost.route) {
            CreatePostScreen (actions.navigateBack)
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
                        navigateToCreatePost=actions.navigateToCreatePost
            )
        }

    }
}

class NavigationActions(private val navController: NavHostController) {

    val navigateToWorkout: (String) -> Unit = { workoutId ->
        TODO("Navigate to workout screen with workoutId $workoutId")
    }
    val navigateToExercise: (String) -> Unit = { exerciseId ->
        TODO("Navigate to exercise screen with exerciseId $exerciseId")
    }
    val navigateToProfile: (String?) -> Unit = { userId ->
        navController.navigate(Screen.Profile.createRoute(userId = userId))
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

    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }

}
