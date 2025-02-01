package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostCreator
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTime

@Preview
@Composable
fun PreviewPostListItem() {
    PostListItem(
        post = FeedPost(
            id = "123",
            content = "Hello world!",
            postCreator = PostCreator(
                id = "123",
                displayName = "John Doe",
                profilePictureUrl = "https://randomuser.me/api/portraits/women/1.jpg"
            ),
            createdAt = System.currentTimeMillis(),
            tags = listOf( "fitness", "workout",),
        ),
        onPostClick = {},
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostListItem(
    post: FeedPost,
    onPostClick: () -> Unit,
) {
    // Post item container
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // Post timestamp
        Text(
            text = formatRelativeTime(post.createdAt),
            fontSize = 12.sp,
            color = Color.Gray
        )

        // Spacer for vertical spacing
        Spacer(modifier = Modifier.height(8.dp))

        // Post content
        Text(
            text = post.content,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Spacer for vertical spacing
        Spacer(modifier = Modifier.height(4.dp))

        // Workout Tags/Chips
        if (post.tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                content = {
                    post.tags.forEach { tag ->
                        // Individual hashtag chip
                        Text(
                            text = "#${tag}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .background(Color(0xFFF5F5F5))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }
            )
        }

        // Spacer for vertical spacing
        Spacer(modifier = Modifier.height(4.dp))

        // Interaction row (likes, comments)
        InteractionRow(
            likeAmount = post.postMetrics.likes,
            commentAmount = post.postMetrics.comments,
        )
    }
}
