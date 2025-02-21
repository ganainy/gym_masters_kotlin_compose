package com.ganainy.gymmasterscompose.ui.theme.screens.post_details


import Comment
import UserDisplayInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.ui.theme.AppTheme
import com.ganainy.gymmasterscompose.ui.theme.shared_components.CenteredText
import com.ganainy.gymmasterscompose.ui.theme.shared_components.post.FeedPostItem
import com.ganainy.gymmasterscompose.ui.theme.models.post.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostCreator
import com.ganainy.gymmasterscompose.ui.theme.models.post.PostMetrics
import java.util.UUID


@Composable
fun DetailedPostItem(
    feedPostWithLikesAndComments: FeedPostWithLikesAndComments,
    onProfileClick: () -> Unit,
    onCommentSubmit: (String) -> Unit,
    isLoadingComments: Boolean = false,
    onPostLikeClick: () -> Unit,
    onCommentLikeClick: (commentId: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        // Post Section
        FeedPostItem(
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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }

        // Comment Input Field
        CommentInputComponent(onCommentSubmit)
    }
}

@Composable
fun PostCommentSection(
    commentList: List<CommentWithLikeStatus>,
    onCommentLikeClick: (commentId: String) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())

    ) {
        // Display each comment
        commentList.forEach { commentWithLikeStatus ->

            CommentComposable(
                commentWithLikeStatus = commentWithLikeStatus,
                onCommentLikeClick = onCommentLikeClick,
            )
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
                timestamp = System.currentTimeMillis(),
                userDisplayInfo = UserDisplayInfo(
                    displayName = "John Doe $it",
                    profileImageUrl = "https://www.example.com/profile.jpg"
                )
            ),
            isLiked = it % 2 == 0
        )
    }


    AppTheme {


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
fun DetailedPostItemNoCommentsPreview() {

    AppTheme {
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

