package com.ganainy.gymmasterscompose.ui.theme.shared_components.user_image

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ganainy.gymmasterscompose.R

@Composable
fun ProfileImageEditable(
    profilePictureUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    onEdit: () -> Unit,
    isOwnProfile: Boolean = true
) {
    var isHovered by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Profile Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(profilePictureUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.profile),
            contentDescription = "Profile Image",
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.profile),
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(
                    BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.surface
                    ),
                    CircleShape
                )
                .clickable(onClick = onEdit)
        )

        // Edit Overlay (only shown if it's the user's own profile)
        if (isOwnProfile) {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .hoverable(
                        interactionSource = remember { MutableInteractionSource() },
                        enabled = true,
                    )
                    .clickable(onClick = onEdit)
                    .background(
                        color = animateColorAsState(
                            if (isHovered) {
                                Color.Black.copy(alpha = 0.6f)
                            } else {
                                Color.Black.copy(alpha = 0.4f)
                            },
                            label = "overlayColor"
                        ).value,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .alpha(
                            animateFloatAsState(
                                if (isHovered) 1f else 0.85f,
                                label = "contentAlpha"
                            ).value
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Update Profile Picture",
                        modifier = Modifier.size(size * 0.3f),
                        tint = Color.White
                    )
                    if (size >= 96.dp) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Update",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = (size.value * 0.12f).sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}