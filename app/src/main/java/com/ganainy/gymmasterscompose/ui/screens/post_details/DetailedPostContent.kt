package com.ganainy.gymmasterscompose.ui.screens.post_details


import Comment
import UserDisplayInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.models.post.PostMetrics
import com.ganainy.gymmasterscompose.ui.screens.feed.composables.ImageGrid
import com.ganainy.gymmasterscompose.ui.screens.feed.composables.UserInfoRow
import com.ganainy.gymmasterscompose.ui.shared_components.CenteredText
import com.ganainy.gymmasterscompose.ui.shared_components.HashtagText
import com.ganainy.gymmasterscompose.ui.shared_components.PostInteractionRow
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTime
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTimeFromFireStoreTimeStamp
import com.google.firebase.Timestamp
import java.util.UUID


@Composable
fun DetailedPostContent(
    feedPostWithLikesAndComments: FeedPostWithLikesAndComments,
    onProfileClick: () -> Unit,
    onCommentSubmit: (String) -> Unit,
    isLoadingComments: Boolean = false,
    onPostLikeClick: () -> Unit,
    onCommentLikeClick: (commentId: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // Scrollable content area
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Column {
                // Post Section
                PostDetailsSection(
                    feedPostWithLikesAndComments = feedPostWithLikesAndComments,
                    onProfileClick = onProfileClick,
                    onLikeIconClick = onPostLikeClick,
                    onPostClick = {},
                )

                //Comments Section
                if (isLoadingComments) {
                    CenteredText(text = "Loading comments...")
                } else {
                    PostCommentSection(
                        feedPostWithLikesAndComments.commentList,
                        onCommentLikeClick = onCommentLikeClick,
                    )
                }
            }
        }

        // Comment Input Field
        CommentInputComponent(onCommentSubmit)
    }
}

@Composable
fun PostDetailsSection(
    feedPostWithLikesAndComments: FeedPostWithLikesAndComments,
    onLikeIconClick: () -> Unit,
    onProfileClick: () -> Unit,
    onPostClick: () -> Unit,
    modifier: Modifier = Modifier
) {
        Column(
            modifier = modifier
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
                                contentScale = ContentScale.Crop,
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

@Composable
fun PostCommentSection(
    commentList: List<CommentWithLikeStatus>,
    onCommentLikeClick: (commentId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)

    ) {
        // Display each comment
        commentList.forEach { commentWithLikeStatus ->

            CommentComposable(
                commentWithLikeStatus = commentWithLikeStatus,
                onCommentLikeClick = onCommentLikeClick,
            )
        }

        if (commentList.isEmpty()) {
            // No comments
            CenteredText(text = "No comments yet")
        }

    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CommentInputComponent(
    onCommentSubmit: (String) -> Unit,
) {
    var newCommentText by remember { mutableStateOf("") } // State for the new comment text

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text Field for Comment Input
        OutlinedTextField(
            value = newCommentText,
            onValueChange = { newCommentText = it },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text("Add a comment...") },
            singleLine = false,
            maxLines = 3,

        )

        // Submit Button
        IconButton(
            onClick = {
                if (newCommentText.isNotBlank()) {
                    onCommentSubmit(newCommentText) // Trigger callback with the new comment
                    newCommentText = "" // Clear the input field
                }
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Submit Comment",
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DetailedPostItemWithCommentsPreview() {

    val commentList =List(5) {
        CommentWithLikeStatus(
            Comment(
                id = UUID.randomUUID().toString(),
                content = "This is a comment $it",
                userId = "user-$it",
                postId = "post-1",
                timestamp = Timestamp.now(),
                userDisplayInfo = UserDisplayInfo(
                    displayName = "John Doe $it",
                    profileImageUrl = "https://www.example.com/profile.jpg"
                )
            ),
            isLiked = it % 2 == 0
        )
    }


    AppTheme {


        DetailedPostContent(
        feedPostWithLikesAndComments = FeedPostWithLikesAndComments(
            post = FeedPost(
                content = "This is a post content",
                postCreator = PostCreator(
                    displayName = "John Doe",
                    profilePictureUrl = "https://www.example.com/profile.jpg"
                ),
                createdAt = Timestamp.now(),
                imageUrlList = emptyList(),
                postMetrics = PostMetrics(
                    likes = 100,
                    comments = 10
                )
            ),
            isLiked = false,
            commentList = commentList
        ),
        onProfileClick = { },
        onCommentSubmit = {},
        isLoadingComments = false,
        onPostLikeClick = {},
        onCommentLikeClick = {},
    )
    }

}

@Preview(showBackground = true)
@Composable
fun DetailedPostContentPreview() {

    AppTheme {
    DetailedPostContent(
        feedPostWithLikesAndComments = FeedPostWithLikesAndComments(
            post = FeedPost(
                content = "This is a post content",
                postCreator = PostCreator(
                    displayName = "John Doe",
                    profilePictureUrl = "https://www.example.com/profile.jpg"
                ),
                createdAt = Timestamp.now(),
                imageUrlList = emptyList(),
                postMetrics = PostMetrics(
                    likes = 100,
                    comments = 10
                )
            ),
            isLiked = false,
            commentList = emptyList()
        ),
        onProfileClick = { },
        onCommentSubmit = {},
        isLoadingComments = false,
        onPostLikeClick = {},
        onCommentLikeClick = {},
    )
}
}

