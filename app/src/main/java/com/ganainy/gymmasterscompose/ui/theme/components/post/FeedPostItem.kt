package com.ganainy.gymmasterscompose.ui.theme.components.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ganainy.gymmasterscompose.animation.LocalShimmerTheme
import com.ganainy.gymmasterscompose.animation.shimmerPlaceholder
import com.ganainy.gymmasterscompose.ui.theme.components.HashtagText
import com.ganainy.gymmasterscompose.ui.theme.components.PostInteractionRow
import com.ganainy.gymmasterscompose.ui.theme.components.UserInfoRow
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostMetrics
import com.ganainy.gymmasterscompose.ui.theme.screens.post_details.FeedPostWithLikesAndComments
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTime


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedPostItem(
    feedPostWithLikesAndComments: FeedPostWithLikesAndComments,
    onLikeIconClick: () -> Unit,
    onProfileClick: () -> Unit,
    onPostClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onPostClick() }
    ) {
        UserInfoRow(
            feedPostWithLikesAndComments.post.postCreator.displayName,
            feedPostWithLikesAndComments.post.postCreator.profilePictureUrl,
            formatRelativeTime(feedPostWithLikesAndComments.post.createdAt),
            onProfileClick = onProfileClick
        )

        HashtagText(
            text = feedPostWithLikesAndComments.post.content,
            onHashtagClick = {}, // TODO: Add hashtag click handler
            modifier = Modifier.padding(vertical = 4.dp)
        )

        if (feedPostWithLikesAndComments.post.imageUrlList.isNotEmpty()) {
            val imageUrls = feedPostWithLikesAndComments.post.imageUrlList.take(4)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                when (imageUrls.size) {
                    1 -> {
                        // Single image takes full width
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrls[0])
                                .crossfade(true)
                                .build(),
                            contentDescription = "Post Image 1",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.5f)
                                .clip(RoundedCornerShape(8.dp))
                                .shimmerPlaceholder(
                                    visible = true,
                                    shimmerTheme = LocalShimmerTheme.current,
                                )
                        )
                    }
                    else -> {
                        // Multiple images in a grid
                        val rows = (imageUrls.size + 1) / 2
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (row in 0 until rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val startIndex = row * 2
                                    for (i in startIndex until minOf(startIndex + 2, imageUrls.size)) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(imageUrls[i])
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Post Image ${i + 1}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .shimmerPlaceholder(
                                                    visible = true,
                                                    shimmerTheme = LocalShimmerTheme.current,
                                                )
                                        )
                                    }
                                    // If odd number of images in the row, add a spacer
                                    if (startIndex + 2 > imageUrls.size && imageUrls.size % 2 != 0) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (feedPostWithLikesAndComments.post.imageUrlList.size > 4) {
                Text(
                    text = "+${feedPostWithLikesAndComments.post.imageUrlList.size - 4} more",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
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



@Preview(showBackground = true)
@Composable
fun PreviewFeedPostItem() {
    FeedPostItem(
        feedPostWithLikesAndComments = FeedPostWithLikesAndComments(
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
