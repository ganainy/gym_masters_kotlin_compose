package com.ganainy.gymmasterscompose.ui.theme.components.user_image

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ganainy.gymmasterscompose.R

@Composable
fun ProfileImageSmall(
    profilePictureUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    onClick: () -> Unit = {}
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(profilePictureUrl)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.profile),
        contentDescription = "Profile Image",
        contentScale = ContentScale.Crop,
        error = painterResource(R.drawable.profile),
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(
                BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.surface
                ),
                CircleShape
            )
            .clickable(onClick = onClick)
    )
}