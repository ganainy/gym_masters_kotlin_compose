package com.ganainy.gymmasterscompose.ui.theme.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ganainy.gymmasterscompose.ui.theme.repository.AuthRepository
import com.ganainy.gymmasterscompose.ui.theme.repository.DataRepository
import com.ganainy.gymmasterscompose.ui.theme.screens.discover.DiscoverScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.feed.FeedScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.signin.SignInScreen
import com.ganainy.gymmasterscompose.ui.theme.screens.signup.SignUpScreen


sealed class Screen(val route: String) {
    object SignUp : Screen("Sign_up")
    object SignIn : Screen("sign_in")
    object Feed : Screen("feed")
    object Discover : Screen("discover")

}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val authRepository = AuthRepository()
    val dataRepository = DataRepository(authRepository)
    val actions = remember(navController) { NavigationActions(navController) }
    NavHost(navController = navController, startDestination = Screen.SignUp.route) {
        composable(route = Screen.SignUp.route) {
            SignUpScreen(
                authRepository,
                actions.navigateToSignIn,
                actions.navigateToFeed,
                actions.navigateBack
            )
        }
        composable(route = Screen.SignIn.route) {
            SignInScreen(
                authRepository, actions.navigateToSignUp,
                actions.navigateToFeed, actions.navigateBack
            )
        }
        composable(route = Screen.Feed.route) {
            FeedScreen(authRepository, actions.navigateToSignIn, actions.navigateToDiscover)
        }
        composable(route = Screen.Discover.route) {
            DiscoverScreen(dataRepository)
        }

    }
}

class NavigationActions(private val navController: NavHostController) {
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
    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }

}
