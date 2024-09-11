package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


@Composable
fun CustomChip(label: String, onClick: () -> Unit = {}) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = label, maxLines = 1,

                modifier = Modifier.wrapContentWidth()
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color(0xFFE0E0E0),
            labelColor = Color.Gray
        ),
        modifier = Modifier.height(24.dp)
    )
}