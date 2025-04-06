package com.ganainy.gymmasterscompose.ui.screens.discover

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.AppTheme
import com.ganainy.gymmasterscompose.ui.shared_components.ProfileImageSmall
import com.ganainy.gymmasterscompose.ui.models.User
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTime
import com.ganainy.gymmasterscompose.utils.Utils.formatRelativeTimeFromFireStoreTimeStamp
import com.google.firebase.Timestamp
import java.util.Date

//todo open user profile on click
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverProfile(
    user: User,
    onFollowClick: () -> Unit,
    isFollowedByLocalUser: Boolean?,
    onProfileClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation states
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    // Animated color for the follow button
    val buttonColor by animateColorAsState(
        targetValue = if (isFollowedByLocalUser == true)
            MaterialTheme.colorScheme.surfaceVariant
        else
            MaterialTheme.colorScheme.primary,
        label = "buttonColor"
    )



        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onProfileClick(user.id) },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Image
                ProfileImageSmall(
                    profilePictureUrl = user.profilePictureUrl,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )

                // User Info Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Display Name
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )

                    // Stats

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Followers Count
                        AnimatedStatsCounter(
                            count = user.stats.followersCount,
                            label = "followers"
                        )

                        Icon(
                            imageVector = Icons.Filled.FiberManualRecord,
                            contentDescription = null,
                            modifier = Modifier.size(8.dp)
                                .align(Alignment.CenterVertically)
                        )

                        // Following Count
                        AnimatedStatsCounter(
                            count = user.stats.followingCount,
                            label = "following"
                        )
                    }

                    // Join Date
                    Text(
                        text = "Joined ${formatRelativeTimeFromFireStoreTimeStamp(user.joinDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Follow Button
                AnimatedFollowIconButton(
                    isFollowed = isFollowedByLocalUser,
                    onClick = onFollowClick,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
}

@Composable
fun AnimatedFollowIconButton(
    isFollowed: Boolean?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    // Scale animation for press effect
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { isPressed = false }
    )

    // Color animation for icon
    val iconTint by animateColorAsState(
        targetValue = if (isFollowed == true)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurface,
        label = "iconTint"
    )

    IconButton(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .scale(scale)
            .size(48.dp)
    ) {
        AnimatedContent(
            targetState = isFollowed,
            label = "followIcon"
        ) { followed ->
            Icon(
                imageVector = ImageVector.vectorResource(
                    id = if (followed == true)
                        R.drawable.person_check_24px
                    else
                        R.drawable.person_add_24px
                ),
                contentDescription = if (followed == true) "Following" else "Follow",
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun AnimatedStatsCounter(
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun AnimatedFollowButton(
    isFollowed: Boolean?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isFollowed == true)
            MaterialTheme.colorScheme.surfaceVariant
        else
            MaterialTheme.colorScheme.primary,
        label = "backgroundColor"
    )

    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (isFollowed == true) "Following" else "Follow",
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDiscoverProfile() {
    AppTheme {
        DiscoverProfile(
            User(
                id = "1234",
                displayName = "amr",
                username = "user1",
                email = "amr@gmail.com",
                joinDate = Timestamp.now(),
            ),
            {},
            true,
            {}
        )
    }
}