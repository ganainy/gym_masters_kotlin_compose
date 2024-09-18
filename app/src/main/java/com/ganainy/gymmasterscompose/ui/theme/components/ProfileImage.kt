package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.AppUtils.generateRandomUsername
import java.util.Date

@Composable
fun ProfileImage(profilePictureUrl: String?) {
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
            .size(48.dp)
            .clip(CircleShape)
            .border(BorderStroke(2.dp, Color.LightGray), CircleShape)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileImage() {
    ProfileImage(
       "asd"
    )
}
