package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun PreviewFeedPostItem() {
    FeedPostItem(
        post = FeedPost(
            id = "123",
            content = "Hello world!",
            postCreator = PostCreator(
                id = "123",
                displayName = "John Doe",
                profilePictureUrl = "https://randomuser.me/api/portraits/women/1.jpg"
            ),
            createdAt = System.currentTimeMillis(),
        ),
        onProfileClick = {},
        onLikeClick = {},
        onCommentClick = { },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedPostItem(
    post: FeedPost,
    onProfileClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    isPostLikedByCurrentUser: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        AccountProfileImage(
            post.postCreator.displayName,
            post.postCreator.profilePictureUrl,
            formatRelativeTime(post.createdAt)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Post content
        HashtagText(
            text = post.content,
            onHashtagClick = {}, // TODO: Add hashtag click handler
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (post.imageUrlList.isNotEmpty()) {
            // Post Image (Placeholder)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                content = {
                    post.imageUrlList.take(4).forEachIndexed { index, url ->
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .height(150.dp)
                                .background(Color.LightGray)
                        )
                    }
                    if (post.imageUrlList.size > 4) {
                        Text(
                            "+${post.imageUrlList.size - 4}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        // todo Like and Comment Section
        PostInteractionRow(
            likeAmount = post.postMetrics.likes,
            onLikeClick = onLikeClick,
            isPostLikedByCurrentUser,
            post.postMetrics.comments,
            onCommentClick = onCommentClick
        )
    }
}