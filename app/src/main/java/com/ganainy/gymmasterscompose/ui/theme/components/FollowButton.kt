package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.R

@Composable
fun FollowButton(isFollowedByLoggedUser: Boolean?, onFollowClick: () -> Unit) {
    Button(
        onClick = onFollowClick,
        modifier = Modifier.height(38.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowedByLoggedUser == true) Color.Gray else MaterialTheme.colorScheme.primary,  // Background color
            contentColor = Color.White  // Text color remains white in both cases
        )
    ) {
        when (isFollowedByLoggedUser) {
            null -> Text(text = stringResource(R.string.loading))
            true -> Text(text = stringResource(R.string.following))
            false -> Text(text = stringResource(R.string.follow))
        }
    }
}
