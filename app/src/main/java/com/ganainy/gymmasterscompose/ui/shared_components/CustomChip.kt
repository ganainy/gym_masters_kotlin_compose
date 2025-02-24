
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomChip(
    label: String,
    onClick: () -> Unit = {},
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Animation for scale effect on selection
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(durationMillis = 200)
    )

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        ),
        color = textColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .scale(animatedScale)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp) // Compact padding
    )
}

@Preview(showBackground = true)
@Composable
fun CustomChipPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomChip(
                label = "Chest",
                isSelected = false,
                onClick = { /* Handle click */ }
            )
            CustomChip(
                label = "Dumbbell",
                isSelected = true,
                onClick = { /* Handle click */ }
            )
            CustomChip(
                label = "Pectoralis Major",
                isSelected = false,
                onClick = { /* Handle click */ }
            )
        }
    }
}