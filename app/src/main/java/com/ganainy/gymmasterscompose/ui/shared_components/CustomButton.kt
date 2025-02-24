package com.ganainy.gymmasterscompose.ui.shared_components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    backgroundGradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
    ),
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    // Animation for button press effect
    val animatedScale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = tween(durationMillis = 200)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp) // Matches typical EditText height
            .scale(animatedScale)
            .clip(RoundedCornerShape(12.dp))
            .shadow(
                elevation = if (enabled) 6.dp else 2.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                brush = if (enabled) backgroundGradient else Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                )
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor.copy(alpha = if (enabled) 1f else 0.5f)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = contentColor.copy(alpha = if (enabled) 1f else 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoolButtonPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enabled button with icon
            CustomButton(
                text = "Save Changes",
                onClick = { /* Handle click */ },
                icon = Icons.Default.Check
            )

            // Enabled button without icon
            CustomButton(
                text = "Submit",
                onClick = { /* Handle click */ }
            )

            // Disabled button with icon
            CustomButton(
                text = "Disabled Button",
                onClick = { /* Handle click */ },
                icon = Icons.Default.Close,
                enabled = false
            )

            // Custom gradient button
            CustomButton(
                text = "Custom Gradient",
                onClick = { /* Handle click */ },
                backgroundGradient = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF6200EA),
                        Color(0xFF3700B3)
                    )
                ),
                contentColor = Color.White
            )
        }
    }
}