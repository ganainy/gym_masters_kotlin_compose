package com.ganainy.gymmasterscompose.ui.theme.screens.discover

import Profile
import Stats
import User
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.SportsMartialArts
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.AppTheme
import com.ganainy.gymmasterscompose.ui.theme.components.ProfileImage
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import java.util.Date

//todo open user profile on click
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverProfile(
    localUser: User,
    onFollowClick: () -> Unit,
    isFollowedByLoggedUser: Boolean?,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                onClick = { }),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //  Profile Image

                ProfileImage(
                    profilePictureUrl = localUser.profile.profilePictureUrl,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(CircleShape),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // User Name with Custom Style
                    localUser.profile.displayName.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Stats Row with Icons
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (localUser.stats.followersCount != null) {
                            StatItem(
                                icon = Icons.Rounded.People,
                                count = localUser.stats.followersCount ?: 0,
                                label = stringResource(R.string.followers)
                            )
                        }

                        if (localUser.stats.workoutCount != null) {
                            StatItem(
                                icon = Icons.Rounded.FitnessCenter,
                                count = localUser.stats.workoutCount,
                                label = stringResource(R.string.workouts)
                            )
                        }

                        if (localUser.stats.exerciseCount != null) {
                            StatItem(
                                icon = Icons.Rounded.SportsMartialArts,
                                count = localUser.stats.exerciseCount,
                                label = stringResource(R.string.exercises)
                            )
                        }

                        val currentLocale = LocalContext.current.resources.configuration.locales[0]
                        val timeAgoMessages = remember(currentLocale) {
                            TimeAgoMessages.Builder().withLocale(currentLocale).build()
                        }

                        JoinDateItem(
                            joinDate = stringResource(
                                R.string.joined,
                                TimeAgo.using(localUser.profile.joinDate, timeAgoMessages)
                            )
                        )
                    }
                }

                // Animated Follow Button
                AnimatedFollowButton(
                    isFollowed = isFollowedByLoggedUser,
                    onClick = onFollowClick
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .width(16.dp)
                .height(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun JoinDateItem(joinDate: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.clock),
            contentDescription = null,
            modifier = Modifier
                .width(16.dp)
                .height(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = joinDate,
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
                profile = Profile(
                    id = "1234",
                    displayName = "amr",
                    username = "user1",
                    email = "amr@gmail.com",
                    joinDate = Date().time,
                ), Stats(3, 2, 3, 19)
            ),
            {},
            true
        )
    }
}