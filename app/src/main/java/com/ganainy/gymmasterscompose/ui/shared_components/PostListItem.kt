package com.ganainy.gymmasterscompose.ui.shared_components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.post.PostCreator
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTime
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTimeFromFireStoreTimeStamp
import com.google.firebase.Timestamp


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostListItem(
    post: FeedPost,
    onPostClick: () -> Unit,
) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable { onPostClick() },
        ) {
            // Post timestamp
            Text(
                text = formatRelativeTimeFromFireStoreTimeStamp(post.createdAt),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Interaction row (likes, comments)
            PostInteractionRow(
                likeAmount = post.postMetrics.likes,
                commentAmount = post.postMetrics.comments,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
}

@Composable
private fun PostInteractionRow(
    likeAmount: Int,
    commentAmount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ThumbUp,
                contentDescription = "Likes",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = likeAmount.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Comment,
                contentDescription = "Comments",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = commentAmount.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewPostListItem() {
    AppTheme {
    PostListItem(
        post = FeedPost(
            id = "123",
            content = "Hello world!",
            postCreator = PostCreator(
                id = "123",
                displayName = "John Doe",
                profilePictureUrl = "https://randomuser.me/api/portraits/women/1.jpg"
            ),
            createdAt = Timestamp.now(),
            tags = listOf( "fitness", "workout",),
        ),
        onPostClick = {},
    )
} }