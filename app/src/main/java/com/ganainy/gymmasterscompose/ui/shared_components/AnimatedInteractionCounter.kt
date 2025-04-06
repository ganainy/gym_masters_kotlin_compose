import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.R
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedInteractionCounter(
    count: Int,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    showZeroCount: Boolean = false,
    debounceTime: Long = 1000L,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var lastClickTime by remember { mutableStateOf(0L) }
    var previousCount by remember { mutableStateOf(count) }
    val countDelta = count - previousCount

    // Update previous count after animation
    LaunchedEffect(count) {
        delay(300)
        previousCount = count
    }

    // Scale animation for the button
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "interaction scale"
    )

    // Animated counter values
    val enterTransition = remember(countDelta) {
        if (countDelta > 0) {
            slideInVertically { height -> height } + fadeIn()
        } else {
            slideInVertically { height -> -height } + fadeIn()
        }
    }

    val exitTransition = remember(countDelta) {
        if (countDelta > 0) {
            slideOutVertically { height -> -height } + fadeOut()
        } else {
            slideOutVertically { height -> height } + fadeOut()
        }
    }

    val debouncedOnClick = {
        val currentTime = Timestamp.now().seconds * 1000 // Convert to milliseconds
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            onClick()
        } else {
            scope.launch {
                Toast.makeText(
                    context,
                    "Please wait a moment before interacting again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        IconButton(
            onClick = { debouncedOnClick() },
            modifier = Modifier.scale(scale)
        ) {
            icon()
        }

        if (count > 0 || showZeroCount) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .padding(start = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = count,
                    transitionSpec = {
                        enterTransition togetherWith exitTransition using SizeTransform(
                            clip = false
                        )
                    },
                    label = "count"
                ) { targetCount ->
                    Text(
                        text = targetCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Specific implementations for Likes and Comments
@Composable
fun AnimatedLikeCounter(
    count: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    showZeroCount: Boolean = false
) {
    AnimatedInteractionCounter(
        count = count,
        isActive = isLiked,
        onClick = onLikeClick,
        modifier = modifier,
        showZeroCount = showZeroCount,
        icon = {
            Icon(
                painter = painterResource(
                    id = if (isLiked) R.drawable.liked_filled else R.drawable.like_outlined
                ),
                contentDescription = "Like",
                tint = if (isLiked) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize)
            )
        }
    )
}

@Composable
fun AnimatedCommentCounter(
    count: Int,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    showZeroCount: Boolean = false
) {
    AnimatedInteractionCounter(
        count = count,
        isActive = false, // Comments don't have an active state
        onClick = onCommentClick,
        modifier = modifier,
        showZeroCount = showZeroCount,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.comment),
                contentDescription = "Comment",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize)
            )
        }
    )
}


@Composable
fun AnimatedSaveCounter(
    count: Int,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSaved: Boolean = false,
    iconSize: Dp = 20.dp,
    showZeroCount: Boolean = true
) {
    AnimatedInteractionCounter(
        count = count,
        isActive = isSaved,
        onClick = onSaveClick,
        modifier = modifier,
        showZeroCount = showZeroCount,
        icon = {
            Icon(
                painter = painterResource(
                    id = if (isSaved) R.drawable.save_filled else R.drawable.save_outlined
                ),
                contentDescription = "Save",
                tint = if (isSaved) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize)
            )
        }
    )
}


