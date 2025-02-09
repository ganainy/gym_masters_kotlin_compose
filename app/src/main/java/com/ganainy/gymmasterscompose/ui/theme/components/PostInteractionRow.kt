package com.ganainy.gymmasterscompose.ui.theme.components

import android.widget.Toast
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.R
import kotlinx.coroutines.launch


@Composable
fun PostInteractionRow(
    likeAmount: Int,
    onLikeClick: () -> Unit = {},
    isLiked: Boolean = false,
    commentAmount: Int,
    onCommentClick: () -> Unit = {},
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var lastClickTime by remember { mutableStateOf(0L) }
    val CLICK_DEBOUNCE_TIME = 1000L

    val debouncedOnLikeClick = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= CLICK_DEBOUNCE_TIME) {
            lastClickTime = currentTime
            onLikeClick()
        }else scope.launch {
            Toast.makeText(context, "Please wait a moment before liking again", Toast.LENGTH_SHORT).show()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = if (isLiked) R.drawable.liked_filled else R.drawable.like_outlined),
                contentDescription = "Like Icon",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { debouncedOnLikeClick() }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = likeAmount.toString(), fontSize = 14.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.comment),
                contentDescription = "Comment Icon",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onCommentClick() }
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