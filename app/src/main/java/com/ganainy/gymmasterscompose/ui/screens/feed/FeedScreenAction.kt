package com.ganainy.gymmasterscompose.ui.screens.feed

import com.ganainy.gymmasterscompose.ui.models.post.FeedPost

sealed interface FeedScreenAction {
    data object RefreshFeed : FeedScreenAction
    data object LoadMorePosts : FeedScreenAction
    data class ToggleReaction(val postId: String) : FeedScreenAction
    data class NavigateToProfile(val userId: String?) : FeedScreenAction
    data object CreatePost : FeedScreenAction
    data class NavigateToDetailedPost(val post: FeedPost, val isLiked: Boolean) : FeedScreenAction
    data object ToggleDrawer : FeedScreenAction
}