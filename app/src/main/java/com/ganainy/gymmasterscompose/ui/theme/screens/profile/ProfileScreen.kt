package com.ganainy.gymmasterscompose.ui.theme.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ganainy.gymmasterscompose.ui.theme.components.FollowButton
import com.ganainy.gymmasterscompose.ui.theme.components.ProfileImage
import com.ganainy.gymmasterscompose.ui.theme.models.FeedPost
import com.ganainy.gymmasterscompose.ui.theme.models.PostStats
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.models.UserStats


@Composable
fun ProfileScreen(
    userId: String?,
    navigateToLogin: () -> Unit,
    navigateToCreatePost: () -> Unit,
) {
    val viewModel = hiltViewModel<ProfileViewModel>()
    viewModel.loadProfile(userId)
    val currentUserId = viewModel.currentUserId // logged in user's ID
    val isOwnProfile = userId == null || userId == currentUserId

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Common UI elements
        ProfileHeader(
            user = uiState.user,
            stats = uiState.stats,
            isOwnProfile = isOwnProfile,
        )

        // Conditional UI elements
        if (isOwnProfile) {
            EditProfileButton(
                onClick = { /* Navigate to edit profile */ }
            )
            LogoutButton(
                onClick = {
                    viewModel.logout {
                        navigateToLogin()
                    }
                }
            )
        } else {
            FollowButton(
                isFollowedByLoggedUser = uiState.isFollowing,
                onFollowClick = { viewModel.toggleFollow(userId) }
            )
            /* MessageButton(
                 onClick = { *//* Navigate to chat *//* }
            )*/
        }

        // Common UI elements
        PostsList(
            posts = uiState.posts,
            onPostClick = { TODO() },
            onCreatePost = navigateToCreatePost,
            modifier = Modifier,
            isOwnProfile = isOwnProfile
        )
    }
}

@Composable
fun ProfileHeader(
    user: User,
    isOwnProfile: Boolean,
    stats: UserStats?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileImage(
            profilePictureUrl = user.profilePictureUrl,
            size = 128.dp,
            onEdit = {
                if (isOwnProfile) {
                    TODO()
                } else null
            },
            isOwnProfile = isOwnProfile
        )

        Text(user.displayName)
        Text(user.bio ?: "")

        if (stats != null)
        StatsRow(stats = stats)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileHeader() {
    ProfileHeader(
        user = User(
            profilePictureUrl = "https://picsum.photos/512/512",
            displayName = "John Doe",
            bio = "Software Engineer"
        ),
        isOwnProfile = true,
        stats = UserStats(
            postCount = 10,
            followersCount = 100,
            followingCount = 50,
            workoutCount = 20
        )
    )
}

@Composable
fun StatsRow(
    stats: UserStats,
    modifier: Modifier = Modifier,
    onStatClick: (StatType) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(
            count = stats.postCount,
            label = "Posts",
            onClick = { onStatClick(StatType.POSTS) }
        )
        StatItem(
            count = stats.followersCount,
            label = "Followers",
            onClick = { onStatClick(StatType.FOLLOWERS) }
        )
        StatItem(
            count = stats.followingCount,
            label = "Following",
            onClick = { onStatClick(StatType.FOLLOWING) }
        )
        StatItem(
            count = stats.workoutCount,
            label = "Workouts",
            onClick = { onStatClick(StatType.WORKOUTS) }
        )
    }
}

@Composable
private fun StatItem(
    count: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

enum class StatType {
    POSTS,
    FOLLOWERS,
    FOLLOWING,
    WORKOUTS
}


@Composable
fun EditProfileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Edit Profile",
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun LogoutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Logout,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Logout",
            style = MaterialTheme.typography.labelLarge
        )
    }
}


@Composable
fun PostsList(
    posts: List<FeedPost>,
    onPostClick: (FeedPost) -> Unit,
    onCreatePost: () -> Unit,
    modifier: Modifier = Modifier,
    isOwnProfile: Boolean = false
) {
    if (posts.isEmpty()) {
        EmptyPostsState(
            isOwnProfile = isOwnProfile,
            onCreatePost = onCreatePost
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = posts,
                key = { it.id }
            ) { post ->
               /* PostItem(
                    post = post,
                    onClick = { onPostClick(post) },
                    postAuthor = TODO(),
                    postStats = TODO(),
                    modifier = TODO()
                )*/
            }
        }
    }
}

@Composable
private fun EmptyPostsState(
    isOwnProfile: Boolean,
    onCreatePost: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.PostAdd,
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Text(
            text = if (isOwnProfile) {
                "Share Your Fitness Journey"
            } else {
                "No Posts Yet"
            },
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (isOwnProfile) {
                "Start sharing your workouts, progress, and inspire others!"
            } else {
                "This user hasn't posted anything yet"
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (isOwnProfile) {
            Button(
                onClick = onCreatePost,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create First Post")
            }
        }
    }
}

@Composable
private fun PostItem(
    post: FeedPost,
    postAuthor: User,
    postStats: PostStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileImage(
                    profilePictureUrl = postAuthor.profilePictureUrl,
                    size = 40.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = postAuthor.displayName ,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = formatTimeAgo(post.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Post Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 12.dp)
            )


            // Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    count = postStats.likes,
                    icon = Icons.Default.Favorite,
                    label = "Likes"
                )
                StatItem(
                    count = postStats.comments,
                    icon = Icons.Default.Comment,
                    label = "Comments"
                )
                StatItem(
                    count = postStats.shares,
                    icon = Icons.Default.Share,
                    label = "Shares"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    count: Int,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun PostMediaContent(
    mediaUrls: List<String>,
    modifier: Modifier = Modifier
) {
    when (mediaUrls.size) {
        0 -> return
        1 -> SingleMediaItem(url = mediaUrls[0])
        2 -> TwoMediaItems(urls = mediaUrls)
        3 -> ThreeMediaItems(urls = mediaUrls)
        4 -> FourMediaItems(urls = mediaUrls)
        else -> GridMediaItems(urls = mediaUrls)
    }
}

@Composable
private fun SingleMediaItem(
    url: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
    )
}

@Composable
private fun TwoMediaItems(urls: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        urls.take(2).forEach { url ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
private fun ThreeMediaItems(urls: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // First large image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(urls[0])
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(0.95f)
                .clip(RoundedCornerShape(8.dp))
        )

        // Two smaller images in a column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            urls.subList(1, 3).forEach { url ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable
private fun FourMediaItems(urls: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            urls.take(2).forEach { url ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }

        // Bottom row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            urls.subList(2, 4).forEach { url ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable
private fun GridMediaItems(urls: List<String>) {
    val remainingCount = urls.size - 4

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // First row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            urls.take(2).forEach { url ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }

        // Second row with overlay for remaining images
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // First two images of second row
            urls.subList(2, 4).forEach { url ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            // Overlay with remaining count
            if (remainingCount > 0) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$remainingCount",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Optional: Add click handling and full-screen preview
@Composable
fun MediaPreview(
    urls: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {

}

// Helper function to format time
private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 1000 * 60 -> "Just now"
        diff < 1000 * 60 * 60 -> "${diff / (1000 * 60)}m ago"
        diff < 1000 * 60 * 60 * 24 -> "${diff / (1000 * 60 * 60)}h ago"
        else -> "${diff / (1000 * 60 * 60 * 24)}d ago"
    }
}
