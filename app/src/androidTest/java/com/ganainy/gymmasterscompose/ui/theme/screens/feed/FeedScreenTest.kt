package com.ganainy.gymmasterscompose.ui.theme.screens.feed

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ganainy.gymmasterscompose.repository.FakeAuthRepository
import com.ganainy.gymmasterscompose.repository.FakeDataRepository
import com.ganainy.gymmasterscompose.ui.theme.AppTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedScreenTest {

    @get:Rule(order = 0)
    val composeTestRule = createComposeRule()

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeDataRepository: FakeDataRepository

    @Before
    fun setup() {
        // Manually create fake instances of your dependencies
        fakeAuthRepository = FakeAuthRepository()
        fakeDataRepository = FakeDataRepository()
        val feedViewModel= FeedViewModel(fakeAuthRepository, fakeDataRepository)
    }

    @Test(timeout = 10000)
    fun testFeedScreen_DisplaysPosts() {
        // Set up your Compose screen
        composeTestRule.setContent {
            AppTheme  {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "feed"
                ) {
                    composable("feed") {
                        FeedScreen(
                            navigateToCreatePost = {},
                            navigateToDiscover = {},
                            navigateToProfile = {},
                            navigateToExercise = {},
                            navigateToWorkout = {}
                        )
                    }
                }
            }


        // Assert that posts are displayed
        composeTestRule.onNodeWithTag("posts_list").assertIsDisplayed()
        composeTestRule.onNodeWithText("This is a test post").assertIsDisplayed()
    }


}
}
