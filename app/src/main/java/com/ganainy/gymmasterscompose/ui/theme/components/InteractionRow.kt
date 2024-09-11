package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
fun InteractionRow(
    likeAmount: Int,
    onLikeClick: () -> Unit,
    isLiked: Boolean,
    commentAmount: Int,
    onCommentClick: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.like), // Placeholder for like icon
                contentDescription = "Like Icon",
                tint = if (isLiked) Color.Blue else Color.Gray,
                modifier = Modifier.clickable { onLikeClick() }

            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = likeAmount.toString(), fontSize = 14.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.comment), // Placeholder for comment icon
                contentDescription = "Comment Icon",
                tint = Color.Gray,
                modifier = Modifier.clickable { onCommentClick() }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = commentAmount.toString(), fontSize = 14.sp)
        }
    }

}

@Preview(showBackground = true)
@Composable
fun PreviewInteractionRow() {
    InteractionRow(5, {}, false, 8, {})
}