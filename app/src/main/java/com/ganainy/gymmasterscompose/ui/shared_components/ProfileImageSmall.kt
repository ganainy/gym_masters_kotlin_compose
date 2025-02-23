package com.ganainy.gymmasterscompose.ui.shared_components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ganainy.gymmasterscompose.animation.ShimmerTheme
import com.ganainy.gymmasterscompose.animation.shimmerPlaceholder

@Composable
fun ProfileImageSmall(
    profilePictureUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    onClick: () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(profilePictureUrl)
                .crossfade(true)
                .size(size.value.toInt() * 2) // 2x for better quality
                .build(),
            contentDescription = "Profile Image",
            contentScale = ContentScale.Crop,
            onLoading = { isLoading = true },
            onSuccess = { isLoading = false },
            onError = {
                isLoading = false
                isError = true
            },
            modifier = Modifier
                .fillMaxSize()
                .shimmerPlaceholder(
                    visible = isLoading,
                    shimmerTheme = ShimmerTheme(
                        dropOff = 0.6f,
                    )
                )
        )

        if (isError || profilePictureUrl == null) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getInitials(profilePictureUrl),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Optional gradient overlay for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0f),
                            Color.Black.copy(alpha = 0.1f)
                        )
                    )
                )
        )
    }
}

private fun getInitials(name: String?): String {
    if (name.isNullOrBlank()) return "?"
    return name.split(" ")
        .take(2)
        .map { it.firstOrNull()?.uppercase() ?: "" }
        .joinToString("")
        .take(2)
}

@Preview
@Composable
fun PreviewProfileImageSmall() {
    ProfileImageSmall(
        profilePictureUrl = "",
        onClick = {}
    )
}