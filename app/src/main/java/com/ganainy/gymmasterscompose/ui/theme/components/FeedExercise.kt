package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeedExercise() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // todo Profile Section (com.ganainy.gymmasterscompose.ui.theme.models.User Image, Name, and Time)
        ProfileHeader("place holder", "place holder")

        Spacer(modifier = Modifier.height(8.dp))

        // Workout Name
        Text(
            text = "Chest workout",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Exercise Tags/Chips using Material3 Chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomChip("Chest")
            CustomChip("Compound")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exercise Images (Placeholder)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(150.dp)
                    .weight(1f)
                    .background(Color.LightGray)
            )
            Box(
                modifier = Modifier
                    .height(150.dp)
                    .weight(1f)
                    .background(Color.LightGray)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // todo Like and Comment Section
        InteractionRow(likeAmount = 0, {}, false, 0, {})

    }
}


@Preview(showBackground = true)
@Composable
fun PreviewFeedExercise() {
    FeedExercise()
}
