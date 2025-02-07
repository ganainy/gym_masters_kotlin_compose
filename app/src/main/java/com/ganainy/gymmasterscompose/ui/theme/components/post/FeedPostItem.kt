package com.ganainy.gymmasterscompose.ui.theme.components.post

import Comment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.components.HashtagText
import com.ganainy.gymmasterscompose.ui.theme.components.PostInteractionRow
import com.ganainy.gymmasterscompose.ui.theme.components.UserInfoRow
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostMetrics
import com.ganainy.gymmasterscompose.ui.theme.screens.feed.FeedPostWithLikes
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTime
import kotlinx.coroutines.flow.Flow


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedPostItem(
    feedPostWithLikesAndComments: FeedPostWithLikes,
    onLikeIconClick: () -> Unit,
    onProfileClick: () -> Unit,
    onPostClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(16.dp)
            .clickable { onPostClick() }
    ) {
        UserInfoRow(
            feedPostWithLikesAndComments.post.postCreator.displayName,
            feedPostWithLikesAndComments.post.postCreator.profilePictureUrl,
            formatRelativeTime(feedPostWithLikesAndComments.post.createdAt),
            onProfileClick = onProfileClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Post content
        HashtagText(
            text = feedPostWithLikesAndComments.post.content,
            onHashtagClick = {}, // TODO: Add hashtag click handler
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (feedPostWithLikesAndComments.post.imageUrlList.isNotEmpty()) {
            // Post Image (Placeholder)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                content = {
                    feedPostWithLikesAndComments.post.imageUrlList.take(4)
                        .forEachIndexed { index, url ->
                            Box(
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(150.dp)
                                    .background(Color.LightGray)
                            )
                        }
                    if (feedPostWithLikesAndComments.post.imageUrlList.size > 4) {
                        Text(
                            "+${feedPostWithLikesAndComments.post.imageUrlList.size - 4}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PostInteractionRow(
            likeAmount = feedPostWithLikesAndComments.post.postMetrics.likes,
            onLikeClick = onLikeIconClick,
            isLiked = feedPostWithLikesAndComments.isLiked,
            feedPostWithLikesAndComments.post.postMetrics.comments,
        )
    }
}


@Composable
fun CommentItem(comment: Comment, onCommentLikeClick: (commentId: String) -> Unit, isCommentLikedFlow: Flow<Boolean>,) {

    val isLiked by isCommentLikedFlow.collectAsState(initial = false)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User profile image
        AsyncImage(
            model = comment.userDisplayInfo.profileImageUrl,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            placeholder = painterResource(id = R.drawable.profile), // Placeholder for missing image
            error = painterResource(id = R.drawable.profile) // Fallback for error
        )

        Spacer(modifier = Modifier.width(8.dp)) // Spacing between image and content

        // Comment content and metadata
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // User name and timestamp
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.userDisplayInfo.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp)) // Spacing between name and timestamp
                Text(
                    text = formatRelativeTime(comment.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp)) // Spacing between name and comment

            // Comment content
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (comment.isPending) Color.Gray else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp)) // Spacing between comment and like button

            // Like button and likes count
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onCommentLikeClick(comment.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) {
                            Icons.Filled.Favorite // Filled heart if liked
                        } else {
                            Icons.Outlined.FavoriteBorder // Outlined heart if not liked
                        },
                        contentDescription = "Like Comment",
                        tint = if (isLiked) {
                            MaterialTheme.colorScheme.error // Red color if liked
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Gray color if not liked
                        }
                    )
                }
                Spacer(modifier = Modifier.width(4.dp)) // Spacing between icon and likes count
                Text(
                    text = "${comment.likesCount} likes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}


@Preview
@Composable
fun PreviewFeedPostItem() {
    FeedPostItem(
        feedPostWithLikesAndComments = FeedPostWithLikes(
            post = FeedPost(
                content = "This is a post content",
                postCreator = PostCreator(
                    displayName = "John Doe",
                    profilePictureUrl = "https://www.example.com/profile.jpg"
                ),
                createdAt = System.currentTimeMillis(),
                imageUrlList = listOf(
                    "https://www.example.com/image1.jpg",
                    "https://www.example.com/image2.jpg",
                    "https://www.example.com/image3.jpg",
                    "https://www.example.com/image4.jpg",
                    "https://www.example.com/image5.jpg",
                    "https://www.example.com/image6.jpg",
                    "https://www.example.com/image7.jpg",
                    "https://www.example.com/image8.jpg",
                    "https://www.example.com/image9.jpg",
                    "https://www.example.com/image10.jpg",
                ),
                postMetrics = PostMetrics(
                    likes = 100,
                    comments = 10
                )
            ),
        ),
        onLikeIconClick = {},
        onProfileClick = { },
        onPostClick = { },
    )
}
