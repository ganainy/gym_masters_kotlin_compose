package com.ganainy.gymmasterscompose.ui.shared_components

import AnimatedCommentCounter
import AnimatedLikeCounter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun PostInteractionRow(
    likeAmount: Int,
    onLikeClick: () -> Unit = {},
    isLiked: Boolean = false,
    commentAmount: Int,
    onCommentClick: () -> Unit = {},
) {

Column {
    HorizontalDivider(
        thickness = 1.dp
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        AnimatedLikeCounter(isLiked = isLiked,
            count = likeAmount,
            onLikeClick = {onLikeClick()},
        )

        Box(
            modifier = Modifier
                .size(1.dp, 24.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        ){
        VerticalDivider(
            thickness = 1.dp
        )}

        AnimatedCommentCounter(
            count = commentAmount,
            onCommentClick = {onCommentClick()},
        )

    }


}
}

@Preview(showBackground = true)
@Composable
fun PreviewPostInteractionRow() {
    PostInteractionRow(5, {}, false, 8, {}, )
}