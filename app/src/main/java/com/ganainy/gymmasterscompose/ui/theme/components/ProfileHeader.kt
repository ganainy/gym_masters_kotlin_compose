package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileHeader(name: String?,imageUrl: String?, timeAgo: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        ProfileImage(imageUrl, Modifier.width(40.dp)
            ,onClick={})
        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = name?:"Unknown",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = timeAgo,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileHeader() {
    ProfileHeader("Amr", "","20 minutes ago")
}