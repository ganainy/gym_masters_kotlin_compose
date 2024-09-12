package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.models.User

@Composable
fun DiscoverProfile(user: User) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.photo)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.profile),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.profile),
                modifier = Modifier
                    .clip(CircleShape)
                    .size(48.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                user.name?.let {
                    Text(
                        text = it,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Build the list of Texts for conditional items
                val textItems = mutableListOf<@Composable () -> Unit>()

                if (user.followers != 0L) {
                    textItems.add {
                        Text(
                            text = stringResource(R.string.followers, user.followers),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                user.workoutsCount?.takeIf { it > 0 }?.let {
                    textItems.add {
                        Text(
                            text = stringResource(R.string.workouts, user.workoutsCount!!),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                user.exercisesCount?.takeIf { it > 0 }?.let {
                    textItems.add {
                        Text(
                            text = stringResource(R.string.exercises, user.exercisesCount!!),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                user.ratingsAverage?.let {
                    textItems.add {
                        Text(
                            text = stringResource(R.string.rating, user.ratingsAverage!!),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Only display the row if there are text items
                if (textItems.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Add bullet points between text items
                        textItems.forEachIndexed { index, textItem ->
                            textItem()
                            if (index < textItems.size - 1) {
                                // Add bullet between items, but not after the last one
                                Text(
                                    text = " • ",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }


        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth() // Make the line fill the width of the parent
                .padding(
                    horizontal = 4.dp,
                    vertical = 8.dp
                ), // Add horizontal and vertical margins
            thickness = 1.dp, // Set the thickness of the line
            color = Color.Gray // Set the color of the line
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDiscoverProfile() {
    DiscoverProfile(User())
}