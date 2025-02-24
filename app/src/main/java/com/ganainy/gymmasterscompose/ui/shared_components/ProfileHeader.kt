package com.ganainy.gymmasterscompose.ui.shared_components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.animation.shimmerPlaceholder
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.models.User
import com.ganainy.gymmasterscompose.ui.models.UserStats

@Composable
fun ProfileHeader(
    user: User,
    isOwnProfile: Boolean,
    stats: UserStats?,
    modifier: Modifier = Modifier,
    onEditProfilePicture: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture with Edit Option
        EditableProfileImage(
            profilePictureUrl = user.profilePictureUrl,
            size = 180.dp,
            isOwnProfile = isOwnProfile,
            onEditProfilePicture = onEditProfilePicture
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Display Name
        Text(
            text = user.displayName,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Bio
        if (!user.bio.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (user.bio.isEmpty()) stringResource(R.string.no_bio) else user.bio.let { bio ->
                    if (bio.length <= 100) bio else bio.substring(
                        0,
                        100
                    )
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // Stats Row
        if (stats != null) {
            Spacer(modifier = Modifier.height(16.dp))
            StatsRow(
                stats = stats,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun StatsRow(stats: UserStats, modifier: Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(label = "Posts", value = stats.postCount.toString())
        StatItem(label = "Followers", value = stats.followersCount.toString())
        StatItem(label = "Following", value = stats.followingCount.toString())
}
}

@Composable
private fun EditableProfileImage(
    profilePictureUrl: String?,
    size: Dp,
    isOwnProfile: Boolean,
    onEditProfilePicture: (String) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 200)
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.toString()?.let { onEditProfilePicture(it) }
        }
    )

    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .clickable(
                onClick = {
                    isPressed = true
                    launcher.launch("image/*")
                },
            ),

    ) {
        // Profile Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(profilePictureUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .fillMaxSize()
                .shimmerPlaceholder(visible = profilePictureUrl == null),
            contentScale = ContentScale.Crop
        )

        // Edit Icon (visible only for own profile)
        if (isOwnProfile) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp) // Square with rounded corners
                    )
                    .clickable(
                        onClick = {
                            isPressed = true
                            launcher.launch("image/*")
                        },
                    )
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile Picture",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}



@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileHeader() {
    AppTheme {
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
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
   }
}
