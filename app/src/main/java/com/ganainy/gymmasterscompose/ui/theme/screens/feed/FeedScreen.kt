package com.ganainy.gymmasterscompose.ui.theme.screens.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.components.LoadingIndicator
import com.ganainy.gymmasterscompose.ui.theme.components.post.FeedPostItem
import com.ganainy.gymmasterscompose.ui.theme.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    navController: NavController,
) {

    //navigation actions
    val handleLogout: () -> Unit = {
        navController.navigate("auth") {
            // Clear the entire back stack when logging out
            popUpTo(0) { inclusive = true }
        }
        }

    val handleCreatePost: () -> Unit = {
        navController.navigate(Screen.CreatePost.route)
    }

    val handleProfile: (String?) -> Unit = { userId ->
        navController.navigate(Screen.Main.Profile.route + "/$userId")
    }

    val handleDiscover: () -> Unit = {
        navController.navigate(Screen.Main.Discover.route)
    }

    val handleExercises: () -> Unit = {
        navController.navigate(Screen.Main.ExerciseList.route)
    }

    val handleCreateWorkout: () -> Unit = {
        navController.navigate(Screen.WorkoutSetup.route)
    }

    val viewModel: FeedViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val feedUiData by viewModel.feedUiData.collectAsState()

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            //todo move create workout to profile screen
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(text = stringResource(R.string.create_workout)) },
                    selected = false,
                    onClick = { handleCreateWorkout() }
                )
            }
    )
    {

        Scaffold(
            topBar = {
                FeedTopBar(
                    onRefresh = { viewModel.refreshFeed() },
                    openMenu = {
                        scope.launch {
                            if (drawerState.isOpen) {
                                drawerState.close()
                            } else {
                                drawerState.open()
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick =   handleCreatePost ,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Post")
                }
            }
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (uiState) {
                    FeedUiState.Loading -> {
                        LoadingIndicator()
                    }

                    FeedUiState.EmptyFeed -> {
                        EmptyFeedMessage()
                    }

                    FeedUiState.NonEmptyFeed -> {
                        LazyColumn(modifier = Modifier.testTag("posts_list")) {
                            items(feedUiData.postList) { feedPostWithLikesAndComments ->
                                FeedPostItem(
                                    feedPostWithLikesAndComments = feedPostWithLikesAndComments,
                                    onProfileClick = { handleProfile(feedPostWithLikesAndComments.post.postCreator.id) },
                                    onLikeIconClick = { viewModel.toggleReaction(feedPostWithLikesAndComments.post.id) },
                                    onCommentIconClick = {viewModel.openComments(feedPostWithLikesAndComments.post.id)} ,
                                    onCommentSubmit = {viewModel.addComment(it,feedPostWithLikesAndComments.post.id)}
                                )
                            }
                            // Load more posts when the last item is visible
                            item {
                                LaunchedEffect(Unit) {
                                    viewModel.loadMorePosts()
                                }
                            }
                        }
                    }

                    is FeedUiState.Error -> ErrorMessage("Error loading feed") { viewModel.refreshFeed() }

                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedTopBar(onRefresh: () -> Unit, openMenu: () -> Unit) {
    TopAppBar(
        title = { Text("Feed") },
        navigationIcon = {
            IconButton(onClick = openMenu) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    )
}


@Composable
private fun ErrorMessage(error: String, refreshFeed: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("error_message"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = refreshFeed,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyFeedMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("empty_feed_message"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Feed,
                contentDescription = null,
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No posts yet",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Follow more users or create your first post",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

