import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.ui.theme.components.CustomChip
import com.ganainy.gymmasterscompose.ui.theme.components.InteractionRow
import com.ganainy.gymmasterscompose.ui.theme.components.ProfileHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutItem() {
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

        // Workout Tags/Chips using Material3 Chip

        FlowRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            content = {
                CustomChip("Chest")
                CustomChip("Intermediate")
                CustomChip("60 Min")
                CustomChip("10 Exercises")
            }
        )



        Spacer(modifier = Modifier.height(16.dp))

        // Workout Image (Placeholder)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // todo Like and Comment Section
        InteractionRow(likeAmount = 0, {}, false, 0, {})

    }
}


@Preview(showBackground = true)
@Composable
fun PreviewWorkoutItem() {
    WorkoutItem()
}
