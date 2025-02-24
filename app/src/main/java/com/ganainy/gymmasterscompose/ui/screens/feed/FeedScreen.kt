package com.ganainy.gymmasterscompose.ui.screens.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.ganainy.gymmasterscompose.ui.screens.post_details.FeedPostWithLikesAndComments
import com.ganainy.gymmasterscompose.ui.shared_components.CustomTopAppBar
import com.ganainy.gymmasterscompose.ui.shared_components.LoadingIndicator
import com.ganainy.gymmasterscompose.ui.theme.screens.feed.composables.FeedPostItem
import com.ganainy.gymmasterscompose.utils.MockData.samplePostWithLikesAndComments
import com.ganainy.gymmasterscompose.utils.MockData.samplePostWithLikesAndCommentsList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    navigateToDetailedPost: (post: FeedPost, isLiked: Boolean) -> Unit,
    navigateToProfile: (userId: String?) -> Unit,
    navigateToCreatePost: () -> Unit,
) {
    // ViewModel and State
    val viewModel: FeedViewModel = hiltViewModel()
    val feedUiData by viewModel.feedUiData.collectAsState()

    // Drawer State
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    FeedScreenContent(
        feedUiData = feedUiData,
        onAction = { action ->
            when (action) {
                is FeedScreenAction.RefreshFeed -> viewModel.refreshFeed()
                is FeedScreenAction.LoadMorePosts -> viewModel.loadNextPage()
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
        },
    )
}


@Composable
private fun FeedScreenContent(
    feedUiData: FeedUiData,
    onAction: (FeedScreenAction) -> Unit,
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
            if (feedUiData.isLoading) {
                LoadingIndicator()
            } else if (feedUiData.postList.isEmpty()) {
                EmptyFeedMessage()
            } else if (feedUiData.error != null) {
                ErrorMessage(feedUiData.error.asString()) { onAction(FeedScreenAction.RefreshFeed) }
            } else {
                FeedPostListComposable(feedUiData.postList, onAction, feedUiData.isRefreshing,feedUiData.hasReachedEnd)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { onAction(FeedScreenAction.CreatePost) },
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Post")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedPostListComposable(
    postList: List<FeedPostWithLikesAndComments>,
    onAction: (FeedScreenAction) -> Unit,
    isRefreshing: Boolean,
    hasReachedEnd:Boolean, //no more posts to load
    modifier: Modifier = Modifier
) {
    // Track list state
    val listState = rememberLazyListState()

    // Track if the user has scrolled
    val hasScrolled = remember { mutableStateOf(false) }

    // Track if we're currently loading more
    var isLoadingMore by remember { mutableStateOf(false) }

    // Detect scroll state to determine if the user has scrolled
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            hasScrolled.value = true
        }
    }

    // Determine if we should load more posts
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItemsCount = layoutInfo.totalItemsCount

            // Only trigger load more if:
            // 1. The user has scrolled (to prevent immediate triggers)
            // 2. There are at least 10 items in the list
            // 3. We're near the end of the list (last 2 items)
            // 4. The list is not empty
            // 5. We're not already loading more or refreshing
            hasScrolled.value &&
                    postList.isNotEmpty() &&
                    postList.size >= 10 &&
                    lastVisibleItemIndex >= totalItemsCount - 2 &&
                    !isLoadingMore &&
                    !isRefreshing
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { onAction(FeedScreenAction.RefreshFeed) },
        modifier = modifier.fillMaxSize(),
        state =pullToRefreshState,
        indicator = {
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                state = pullToRefreshState
            )
        }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.testTag("posts_list")
        ) {
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

            // Loading more indicator
            item {
                if (!isRefreshing && postList.isNotEmpty()) {
                    // Trigger load more when needed
                    LaunchedEffect(shouldLoadMore.value) {
                        if (shouldLoadMore.value) {
                            isLoadingMore = true
                            onAction(FeedScreenAction.LoadMorePosts)
                            // Delay to debounce rapid triggers
                            delay(500L)
                            isLoadingMore = false
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoadingMore) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        } else if (hasReachedEnd) {
                            Text(
                                text = "No more posts to load",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
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
fun FeedScreenContentPreview_NonEmptyFeed() {
    AppTheme {
        FeedScreenContent(
            feedUiData = FeedUiData(postList = samplePostWithLikesAndCommentsList),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenContentPreview_Loading() {
    AppTheme {
        FeedScreenContent(
            feedUiData = FeedUiData(
                postList = samplePostWithLikesAndCommentsList,
                isLoading = true
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenContentPreview_Refreshing() {
    AppTheme {
        FeedScreenContent(
            feedUiData = FeedUiData(
                postList = listOf(samplePostWithLikesAndComments),
                isLoading = false,
                isRefreshing = true
            ),
            onAction = {},
        )
    }
}


@Preview(showBackground = true)
@Composable
fun FeedScreenContentPreview_NoMorePostsToLoad() {
    AppTheme {
        FeedScreenContent(
            feedUiData = FeedUiData(
                postList = listOf(samplePostWithLikesAndComments),
                isLoading = false,
                isRefreshing = false,
                hasReachedEnd = true
            ),
            onAction = {},
        )
    }
}
