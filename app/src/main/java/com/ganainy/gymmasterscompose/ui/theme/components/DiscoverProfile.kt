package com.ganainy.gymmasterscompose.ui.theme.components

import LocalUser
import User
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.theme.AppUtils.generateRandomUsername
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverProfile(
    localUser: LocalUser,
    onFollowClick: () -> Unit,
    isFollowedByLoggedUser: Boolean?
) {
    //todo open user profile on click
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,

            ) {

            ProfileImage(localUser.user?.profilePictureUrl)

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                localUser.user?.name?.let {
                    Text(
                        text = it,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Build the list of Texts for conditional items
                val textItems = mutableListOf<@Composable () -> Unit>()

                if (localUser.followersCount != 0) {
                    textItems.add {
                        Text(
                            text = stringResource(R.string.followers, localUser.followersCount),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                localUser.workoutCount.takeIf { it > 0 }?.let {
                    textItems.add {
                        Text(
                            text = stringResource(R.string.workouts, localUser.workoutCount),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                localUser.exerciseCount.takeIf { it > 0 }?.let {
                    textItems.add {
                        Text(
                            text = stringResource(R.string.exercises, localUser.exerciseCount),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                localUser.averageRating?.let {
                    textItems.add {
                        Text(
                            text = stringResource(R.string.rating, localUser.averageRating),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Only display the row if there are text items
                if (textItems.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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

            FollowButton(
                onFollowClick = onFollowClick,
                isFollowedByLoggedUser = isFollowedByLoggedUser
            )


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
    DiscoverProfile(
        LocalUser(
            user = User(
                name = "amr",
                userId = "user1",
                email = "amr@gmail.com",
                joinDate = Date().time,
                username = generateRandomUsername(),
            ), exerciseCount = 3, workoutCount = 2, averageRating = "3", followersCount = 10
        ),
        {},
        true
    )
}