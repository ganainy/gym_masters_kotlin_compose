package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.ui.theme.models.User
import com.ganainy.gymmasterscompose.ui.theme.models.UserStats
import com.ganainy.gymmasterscompose.ui.theme.screens.profile.StatsRow

@Composable
fun AccountProfileImage(
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
fun PreviewAccountProfileImage() {
    AccountProfileImage(
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
