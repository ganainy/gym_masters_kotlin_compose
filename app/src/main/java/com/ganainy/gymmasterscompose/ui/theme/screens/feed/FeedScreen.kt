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
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.ui.theme.AppTheme
import com.ganainy.gymmasterscompose.ui.theme.components.LoadingIndicator
import com.ganainy.gymmasterscompose.ui.theme.components.post.FeedPostItem
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostMetrics
import com.ganainy.gymmasterscompose.ui.theme.screens.post_details.FeedPostWithLikesAndComments
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    navigateToDetailedPost: (post: FeedPost, isLiked: Boolean) -> Unit,
    navigateToProfile: (userId: String?) -> Unit,
    navigateToCreatePost: () -> Unit,
) {
    // ViewModel and State
    val viewModel: FeedViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val feedUiData by viewModel.feedUiData.collectAsState()

    // Drawer State
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    FeedScreenContent(
        uiState = uiState,
        feedUiData = feedUiData,
        drawerState = drawerState,
        scope = scope,
        onAction = { action ->
            when (action) {
                is FeedScreenAction.RefreshFeed -> viewModel.refreshFeed()
                is FeedScreenAction.LoadMorePosts -> viewModel.loadMorePosts()
                is FeedScreenAction.ToggleReaction -> viewModel.toggleReaction(action.postId)
                is FeedScreenAction.NavigateToProfile ->
                    navigateToProfile(action.userId)
                is FeedScreenAction.CreatePost ->
                    navigateToCreatePost()
                is FeedScreenAction.NavigateToDetailedPost ->
                    navigateToDetailedPost(action.post, action.isLiked)
                is FeedScreenAction.ToggleDrawer -> scope.launch {
                    if (drawerState.isOpen) drawerState.close() else drawerState.open()
                }
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun FeedScreenContentPreview() {
    val previewScope = rememberCoroutineScope()
    val previewDrawerState = rememberDrawerState(DrawerValue.Closed)

    // First post without images
    val firstPost = FeedPost(
        id = "1",
        content = "Just finished an amazing workout! 💪 #fitness #motivation",
        imagePathList = emptyList(),
        imageUrlList = emptyList(),
        createdAt = System.currentTimeMillis(),
        tags = listOf("fitness", "motivation"),
        postMetrics = PostMetrics(postId = "1", likes = 42, comments = 7),
        postCreator = PostCreator(
            id = "user1",
            displayName = "John Doe",
            profilePictureUrl = "https://picsum.photos/200"
        )
    )

    // Second post with images
    val secondPost = FeedPost(
        id = "2",
        content = "Check out my workout progress! 🏋️‍♂️ #gym #progress",
        imagePathList = emptyList(),
        imageUrlList = listOf(
            "https://picsum.photos/800/600",
            "https://picsum.photos/800/601",
            "https://picsum.photos/800/601",
            "https://picsum.photos/800/601",
            "https://picsum.photos/800/601",
            "https://picsum.photos/800/601",
        ),
        createdAt = System.currentTimeMillis() - 3600000, // 1 hour ago
        tags = listOf("gym", "progress"),
        postMetrics = PostMetrics(postId = "2", likes = 88, comments = 12),
        postCreator = PostCreator(
            id = "user2",
            displayName = "Jane Smith",
            profilePictureUrl = "https://picsum.photos/201"
        )
    )

    val previewFeedUiData = FeedUiData(
        postList = listOf(
            FeedPostWithLikesAndComments(post = firstPost),
            FeedPostWithLikesAndComments(post = secondPost)
        ),
        followingUserIds = emptySet(),
        lastLoadedPostTimestamp = 0L
    )

AppTheme {
    FeedScreenContent(
        uiState = FeedUiState.NonEmptyFeed,
        feedUiData = previewFeedUiData,
        drawerState = previewDrawerState,
        scope = previewScope,
        onAction = {}
    )
}

}

@Composable
private fun FeedScreenContent(
    uiState: FeedUiState,
    feedUiData: FeedUiData,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onAction: (FeedScreenAction) -> Unit
) {
    Scaffold(
        topBar = {
            FeedTopBar(
                onRefresh = { onAction(FeedScreenAction.RefreshFeed)  },
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
                onClick = { onAction(FeedScreenAction.CreatePost)  },
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
                                onProfileClick = {  onAction(FeedScreenAction.NavigateToProfile(feedPostWithLikesAndComments.post.postCreator.id)) },
                                onLikeIconClick = {onAction(FeedScreenAction.ToggleReaction(feedPostWithLikesAndComments.post.id))
                                },
                                onPostClick = {
                                    onAction(FeedScreenAction.NavigateToDetailedPost(
                                        feedPostWithLikesAndComments.post,
                                        feedPostWithLikesAndComments.isLiked
                                    ))
                                }
                            )
                        }
                        // Load more posts when the last item is visible
                        item {
                            LaunchedEffect(Unit) {
                                onAction(FeedScreenAction.LoadMorePosts)
                            }
                        }
                    }
                }

                is FeedUiState.Error -> ErrorMessage("Error loading feed") { onAction(FeedScreenAction.RefreshFeed) }

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

