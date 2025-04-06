package com.ganainy.gymmasterscompose.ui.screens.feed.composables


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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ganainy.gymmasterscompose.animation.LocalShimmerTheme
import com.ganainy.gymmasterscompose.animation.shimmerPlaceholder
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.models.post.PostMetrics
import com.ganainy.gymmasterscompose.ui.screens.post_details.FeedPostWithLikesAndComments
import com.ganainy.gymmasterscompose.ui.shared_components.HashtagText
import com.ganainy.gymmasterscompose.ui.shared_components.PostInteractionRow
import com.ganainy.gymmasterscompose.ui.shared_components.ProfileImageSmall
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTime
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTimeFromFireStoreTimeStamp
import com.google.firebase.Timestamp


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
            formatRelativeTimeFromFireStoreTimeStamp(feedPostWithLikesAndComments.post.createdAt),
            onProfileClick = onProfileClick
        )

        HashtagText(
            text = feedPostWithLikesAndComments.post.content,
            onHashtagClick = {}, // TODO: Add hashtag click handler
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
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
                        ImageGrid(
                            imageUrls = imageUrls,
                            modifier = Modifier.padding(16.dp)
                        )
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
            onCommentClick = onPostClick, //when comment icon is clicked show post details
        )

    }
}

/**
 * Displays a grid of images with 2 images per row. If the total number of images is odd,
 * the last image will span the full width of the row.
 *
 * @param imageUrls List of image URLs to display
 * @param modifier Modifier for the root Column composable
 */
@Composable
fun ImageGrid(
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    // Calculate the number of complete rows (2 images per row)
    // For odd number of images, we'll handle the last image separately
    val completeRows = imageUrls.size / 2
    // Check if there's a remaining image (odd count)
    val hasExtraImage = imageUrls.size % 2 != 0

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Display complete rows (2 images each)
        for (row in 0 until completeRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Display two images in each row
                for (i in row * 2 until row * 2 + 2) {
                    ImageItem(
                        imageUrl = imageUrls[i],
                        contentDescription = "Post Image ${i + 1}",
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    )
                }
            }
        }

        // Handle the last image if count is odd
        if (hasExtraImage) {
            // Display the last image full-width
            ImageItem(
                imageUrl = imageUrls.last(),
                contentDescription = "Post Image ${imageUrls.size}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
            )
        }
    }
}

/**
 * Displays a single image with loading shimmer effect.
 *
 * @param imageUrl URL of the image to load
 * @param contentDescription Description for accessibility
 * @param modifier Modifier for the image composable
 */
@Composable
private fun ImageItem(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .shimmerPlaceholder(
                visible = true,
                shimmerTheme = LocalShimmerTheme.current,
            )
    )
}

@Composable
fun UserInfoRow(name: String?, imageUrl: String?, timeAgo: String, onProfileClick: () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        ProfileImageSmall(imageUrl,onClick=onProfileClick)
        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = name?:"Unknown",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onProfileClick() }
            )
            Text(
                text = timeAgo,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
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
                createdAt = Timestamp.now(),
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

@Preview(showBackground = true)
@Composable
fun PreviewFeedPostItem2() {
    FeedPostItem(
        feedPostWithLikesAndComments = FeedPostWithLikesAndComments(
            post = FeedPost(
                content = "This is a post content",
                postCreator = PostCreator(
                    displayName = "John Doe",
                    profilePictureUrl = "https://www.example.com/profile.jpg"
                ),
                createdAt = Timestamp.now(),
                imageUrlList = listOf(
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

