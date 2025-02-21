package com.ganainy.gymmasterscompose.animation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned

data class ShimmerTheme(
    val baseColor: Color = Color.LightGray.copy(alpha = 0.5f),
    val highlightColor: Color = Color.White,
    val durationMillis: Int = 1000,
    val dropOff: Float = 0.5f,
    val tilt: Float = 20f
)

val LocalShimmerTheme = compositionLocalOf { ShimmerTheme() }

@Composable
fun Modifier.shimmerPlaceholder(
    visible: Boolean,
    shimmerTheme: ShimmerTheme = LocalShimmerTheme.current,
) = composed {
    if (visible) {
        var width by remember { mutableFloatStateOf(0f) }
        var height by remember { mutableFloatStateOf(0f) }
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation = transition.animateFloat(
            initialValue = -2 * width,
            targetValue = 2 * width,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = shimmerTheme.durationMillis),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer translation"
        )
        background(
            brush = Brush.linearGradient(
                colors = listOf(
                    shimmerTheme.baseColor,
                    shimmerTheme.highlightColor,
                    shimmerTheme.baseColor
                ),
                start = Offset(translateAnimation.value - width, 0f),
                end = Offset(translateAnimation.value, height)
            )
        )
            .onGloballyPositioned {
                width = it.size.width.toFloat()
                height = it.size.height.toFloat()
            }
    } else {
        this
    }
}