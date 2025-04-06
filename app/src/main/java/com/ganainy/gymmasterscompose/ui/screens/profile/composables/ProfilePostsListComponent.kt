package com.ganainy.gymmasterscompose.ui.screens.profile.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.models.post.PostMetrics
import com.ganainy.gymmasterscompose.ui.screens.profile.EmptyStateActionType
import com.ganainy.gymmasterscompose.ui.shared_components.PostListItem
import com.google.firebase.Timestamp

@Composable
internal fun ProfilePostsListComponent(
    posts: List<FeedPost>,
    onPostClick: (FeedPost) -> Unit,
    onCreatePost: () -> Unit,
    modifier: Modifier = Modifier,
    isOwnProfile: Boolean = false
) {
    if (posts.isEmpty()) {
        EmptyStateComponent(
            isOwnProfile = isOwnProfile,
            onAction = onCreatePost,
            actionType = EmptyStateActionType.CREATE_POST_ACTION,
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = posts,
                key = { it.id }
            ) { post ->
                PostListItem(
                    post = post,
                    onPostClick = { onPostClick(post) },
                )
                if (post != posts.last()) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun PreviewProfilePostsListComponent() {
    val samplePosts = listOf(
        FeedPost(
            id = "1",
            content = "Sample post content 1",
            imagePathList = emptyList(),
            imageUrlList = emptyList(),
            createdAt = Timestamp.now(),
            tags = listOf("Sample", "Post"),
            postMetrics = PostMetrics("1", likes = 10, comments = 5, shares = 2),
            postCreator = PostCreator("1", "John Doe", "https://example.com/profile.jpg")
        ),
        FeedPost(
            id = "2",
            content = "Sample post content 2",
            imagePathList = emptyList(),
            imageUrlList = emptyList(),
            createdAt = Timestamp.now(),
            tags = listOf("Sample", "Post"),
            postMetrics = PostMetrics("2", likes = 20, comments = 10, shares = 5),
            postCreator = PostCreator("2", "Jane Smith", "https://example.com/profile2.jpg")
        )
    )

    ProfilePostsListComponent(
        posts = samplePosts,
        onPostClick = {},
        onCreatePost = {},
        isOwnProfile = true
    )
}