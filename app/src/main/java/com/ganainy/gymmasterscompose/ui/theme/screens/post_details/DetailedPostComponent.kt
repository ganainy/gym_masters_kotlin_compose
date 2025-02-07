package com.ganainy.gymmasterscompose.ui.theme.screens.post_details


import Comment
import UserDisplayInfo
import androidx.compose.foundation.background
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.ui.theme.components.CenteredText
import com.ganainy.gymmasterscompose.ui.theme.components.HashtagText
import com.ganainy.gymmasterscompose.ui.theme.components.PostInteractionRow
import com.ganainy.gymmasterscompose.ui.theme.components.UserInfoRow
import com.ganainy.gymmasterscompose.ui.theme.components.post.CommentItem
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostMetrics
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailedPostItem(
    feedPostWithLikesAndComments: FeedPostWithLikesAndComments,
    onProfileClick: () -> Unit,
    onCommentSubmit: (String) -> Unit,
    isLoadingComments: Boolean = false,
    onPostLikeClick: () -> Unit,
    onCommentLikeClick: (commentId: String) -> Unit,
    isCommentLiked: (commentId: String, postId: String) -> Flow<Boolean>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(16.dp)
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
            onLikeClick = onPostLikeClick,
            isLiked = feedPostWithLikesAndComments.isLiked,
            feedPostWithLikesAndComments.post.postMetrics.comments,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoadingComments) {
            // Loading comments indicator
            CenteredText(text = "Loading comments...")
        } else {
            // Comment Section
            PostCommentSection(
                feedPostWithLikesAndComments.commentList,
                onCommentLikeClick = onCommentLikeClick,
                isCommentLiked = isCommentLiked,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }

        // Comment Input Field
        CommentInputComponent(onCommentSubmit)
    }
}


@Composable
fun PostCommentSection(
    commentList: List<Comment>,
    onCommentLikeClick: (commentId: String) -> Unit,
    isCommentLiked: (commentId: String, postId: String) -> Flow<Boolean>,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())

    ) {
        // Display each comment
        commentList.forEach { comment ->
            val isCommentLikedFlow = isCommentLiked(comment.id, comment.postId)
            CommentItem(comment = comment,onCommentLikeClick=onCommentLikeClick,isCommentLikedFlow=isCommentLikedFlow)
            Spacer(modifier = Modifier.height(8.dp)) // Add spacing between comments
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
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
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
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Preview
@Composable
fun DetailedPostItemPreview() {
    DetailedPostItem(
        feedPostWithLikesAndComments = FeedPostWithLikesAndComments(
            post = FeedPost(
                content = "This is a post content",
                postCreator = PostCreator(
                    displayName = "John Doe",
                    profilePictureUrl = "https://www.example.com/profile.jpg"
                ),
                createdAt = System.currentTimeMillis(),
                imageUrlList = emptyList(),
                postMetrics = PostMetrics(
                    likes = 100,
                    comments = 10
                )
            ),
            isLiked = false,
            commentList = List(0) {
                Comment(
                    id = UUID.randomUUID().toString(),
                    content = "This is a comment $it",
                    userId = "user-$it",
                    postId = "post-1",
                    timestamp = System.currentTimeMillis(),
                    userDisplayInfo = UserDisplayInfo(
                        displayName = "John Doe $it",
                        profileImageUrl = "https://www.example.com/profile.jpg"
                    )
                )
            }
        ),
        onProfileClick = { },
        onCommentSubmit = {},
        isLoadingComments = false,
        onPostLikeClick = {},
        onCommentLikeClick = {},
        isCommentLiked = { commentId: String, postId: String -> flow { emit(false) } }
    )
}

