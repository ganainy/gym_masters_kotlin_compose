package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.R


@Composable
fun PostInteractionRow(
    likeAmount: Int,
    onLikeClick: () -> Unit = {},
    isLiked: Boolean = false,
    commentAmount: Int,
    onCommentClick: () -> Unit = {},
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = if (isLiked) R.drawable.liked_filled else R.drawable.like_outlined ), // Placeholder for like icon
                contentDescription = "Like Icon",
                modifier = Modifier.size(24.dp).clickable { onLikeClick() }

            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = likeAmount.toString(), fontSize = 14.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.comment), // Placeholder for comment icon
                contentDescription = "Comment Icon",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp).clickable { onCommentClick() }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = commentAmount.toString(), fontSize = 14.sp)
        }
    }

}

@Preview(showBackground = true)
@Composable
fun PreviewPostInteractionRow() {
    PostInteractionRow(5, {}, false, 8, {}, )
}