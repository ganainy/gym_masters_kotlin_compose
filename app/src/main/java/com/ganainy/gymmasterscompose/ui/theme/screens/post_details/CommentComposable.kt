package com.ganainy.gymmasterscompose.ui.theme.screens.post_details

import AnimatedLikeCounter
import Comment
import UserDisplayInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.ui.theme.shared_components.user_image.ProfileImageSmall
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTime

@Composable
fun CommentComposable(
    commentWithLikeStatus: CommentWithLikeStatus,
    onCommentLikeClick: (commentId: String) -> Unit,
    modifier: Modifier = Modifier
) {



    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // User profile image with clickable modifier
            ProfileImageSmall(
                profilePictureUrl = commentWithLikeStatus.comment.userDisplayInfo.profileImageUrl,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Comment content and metadata
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // User name and timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = commentWithLikeStatus.comment.userDisplayInfo.displayName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatRelativeTime(commentWithLikeStatus.comment.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Comment content with selectable text
                SelectionContainer {
                    Text(
                        text = commentWithLikeStatus.comment.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Like button and count with animations
                    AnimatedLikeCounter(isLiked = commentWithLikeStatus.isLiked,
                        count = commentWithLikeStatus.comment.likesCount,
                        onLikeClick = {onCommentLikeClick(commentWithLikeStatus.comment.id)},
                    )

            }
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun CommentComposablePreview() {
    val sampleComment = CommentWithLikeStatus(
        comment = Comment(
            id = "1",
            content = "This is a sample comment with some longer text to see how it wraps and looks in the UI.",
            userDisplayInfo = UserDisplayInfo(
                displayName = "John Doe",
                profileImageUrl = null
            ),
            timestamp = System.currentTimeMillis() - 3600000,
            likesCount = 42
        ),
        isLiked = true
    )

    CommentComposable(
        commentWithLikeStatus = sampleComment,
        onCommentLikeClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCommentComposable() {
    CommentComposable(
        commentWithLikeStatus = CommentWithLikeStatus(
            comment = Comment(
                id = "comment-1",
                content = "short comment with no likes",
                userId = "user-1",
                postId = "post-1",
                timestamp = System.currentTimeMillis(),
                userDisplayInfo = UserDisplayInfo(
                    displayName = "John Doe",
                    profileImageUrl = "https://www.example.com/profile.jpg"
                )
            ),
            isLiked = false
        ),
        onCommentLikeClick = { }
    )
}
