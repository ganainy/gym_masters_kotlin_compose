package com.ganainy.gymmasterscompose.ui.theme.components

import Profile
import Stats
import User
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTime
import com.ganainy.gymmasterscompose.ui.theme.models.FeedPost

@Composable
@Preview
private fun PreviewPostCard() {
    PostCard(
        post = FeedPost(
            id = "123",
            author = User(
                Profile(
                    id = "123",
                    username = "ninja123",
                    displayName = "Ganainy",
                    profilePictureUrl = "https://picsum.photos/200"
                ),
                stats = Stats()
            ),
            content = "Hello world!",
            linkedExerciseId = "123",
            linkedWorkoutId = null,

        ),
        onProfileClick = {},
        onExerciseClick = null,
        onWorkoutClick = null,
        onLikeClick = {}
    )
}
@Composable
fun PostCard(
    post: FeedPost,
    onProfileClick: () -> Unit,
    onExerciseClick: (() -> Unit)?,
    onWorkoutClick: (() -> Unit)?,
    onLikeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // Author Header
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            ProfileImage(post.author?.profile?.profilePictureUrl, Modifier.width(40.dp)
            ,onClick=onProfileClick)

            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = post.author?.profile?.displayName ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatRelativeTime(post.createdAt),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Content
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = post.content)

        // Media Content
        if (post.mediaUrls.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(post.mediaUrls) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Post media",
                        modifier = Modifier
                            .height(200.dp)
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }

        // Linked Content
        post.linkedExercise?.let { exercise ->
            if (onExerciseClick != null) {
                LinkedContentChip(
                    text = "Exercise: ${exercise.name}",
                    onRemove = onExerciseClick
                )
            }
        }

        post.linkedWorkout?.let { workout ->
            if (onWorkoutClick != null) {
                LinkedContentChip(
                    text = "Workout: ${workout.title}",
                    onRemove = onWorkoutClick
                )
            }
        }

        // Stats and Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = if (post.currentUserReaction != null) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = "Like",
                    tint = if (post.currentUserReaction != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        LocalContentColor.current
                    }
                )
            }
            Text("${post.stats.likes} likes")
            Text("${post.stats.comments} comments")
            Text("${post.stats.shares} shares")
        }
    }
}