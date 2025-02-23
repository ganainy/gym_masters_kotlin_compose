package com.ganainy.gymmasterscompose.ui.screens.feed

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.shared_components.CustomTopAppBar
import com.ganainy.gymmasterscompose.ui.shared_components.LoadingIndicator
import com.ganainy.gymmasterscompose.ui.theme.screens.feed.composables.FeedPostItem
import com.ganainy.gymmasterscompose.utils.MockData.samplePostWithLikesAndCommentsList
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


@Composable
private fun FeedScreenContent(
    uiState: FeedUiState,
    feedUiData: FeedUiData,
    onAction: (FeedScreenAction) -> Unit
) {

        Column {
            CustomTopAppBar(
                title = stringResource(R.string.feed),
                actionIcons = listOf(Icons.Default.Refresh),
                onActionClicks = listOf { onAction(FeedScreenAction.RefreshFeed) },
                    )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize() .padding(16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingActionButton(
                        onClick = { onAction(FeedScreenAction.CreatePost) },
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Post")
                    }
                }

                when (uiState) {
                    FeedUiState.Loading -> {
                        LoadingIndicator()
                    }

                    FeedUiState.EmptyFeed -> {
                        EmptyFeedMessage()
                    }

                    is FeedUiState.NonEmptyFeed -> {
                        val postList = uiState.postList

                        LazyColumn(modifier = Modifier.testTag("posts_list")) {
                            items(postList) { feedPostWithLikesAndComments ->
                                FeedPostItem(
                                    feedPostWithLikesAndComments = feedPostWithLikesAndComments,
                                    onProfileClick = {
                                        onAction(
                                            FeedScreenAction.NavigateToProfile(
                                                feedPostWithLikesAndComments.post.postCreator.id
                                            )
                                        )
                                    },
                                    onLikeIconClick = {
                                        onAction(
                                            FeedScreenAction.ToggleReaction(
                                                feedPostWithLikesAndComments.post.id
                                            )
                                        )
                                    },
                                    onPostClick = {
                                        onAction(
                                            FeedScreenAction.NavigateToDetailedPost(
                                                feedPostWithLikesAndComments.post,
                                                feedPostWithLikesAndComments.isLiked
                                            )
                                        )
                                    }
                                )
                                if (postList.last() != feedPostWithLikesAndComments) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                            // Load more posts when the last item is visible
                            item {
                                LaunchedEffect(Unit) {
                                    onAction(FeedScreenAction.LoadMorePosts)
                                }
                            }
                        }
                    }

                    is FeedUiState.Error -> ErrorMessage("Error loading feed") {
                        onAction(
                            FeedScreenAction.RefreshFeed
                        )
                    }


                }
            }
        }
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


@Preview(showBackground = true)
@Composable
fun FeedScreenContentPreview() {

    val previewFeedUiData = FeedUiData(
        followingUserIds = emptySet(),
        lastLoadedPostTimestamp = 0L
    )

    AppTheme {
        FeedScreenContent(
            uiState = FeedUiState.NonEmptyFeed(samplePostWithLikesAndCommentsList),
            feedUiData = previewFeedUiData,
            onAction = {}
        )
    }

}
