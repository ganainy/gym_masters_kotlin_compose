package com.ganainy.gymmasterscompose.ui.shared_components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null, // For vector icons
    navigationIconDrawable: Int? = null, // For drawable resources
    onNavigationClick: () -> Unit = {},

    actionIcons: List<ImageVector> = emptyList(), // For vector icons
    disabledActionIcons: List<ImageVector> = emptyList(), // For vector icons
    actionDrawables: List<Int> = emptyList(), // For drawable resources
    onActionClicks: List<() -> Unit> = emptyList() // Click handlers for actions
) {
    Column {
        TopAppBar(
            modifier = modifier,
            title = { Text(title) },
            navigationIcon = {
                // Handle navigation icon (either vector or drawable)
                when {
                    navigationIcon != null -> {
                        IconButton(onClick = onNavigationClick) {
                            Icon(
                                imageVector = navigationIcon,
                                contentDescription = "Navigation icon"
                            )
                        }
                    }

                    navigationIconDrawable != null -> {
                        IconButton(onClick = onNavigationClick) {
                            Icon(
                                painter = painterResource(id = navigationIconDrawable),
                                contentDescription = "Navigation icon"
                            )
                        }
                    }
                }
            },
            actions = {
                // Handle action icons (vectors)
                actionIcons.forEachIndexed { index, icon ->
                    // Ensure we have a corresponding click handler
                    if (index < onActionClicks.size) {
                        val isDisabled = disabledActionIcons.contains(icon)

                        IconButton(
                            onClick = { onActionClicks[index]() },
                            enabled = !isDisabled // Disable button if icon is in disabled set
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = when (icon) {
                                    Icons.Filled.PostAdd -> "Publish Post"
                                    // Add more specific descriptions for other icons
                                    else -> "Action $index"
                                },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = if (isDisabled) 0.38f else 1f
                                )
                            )
                        }
                    }
                }
                // Handle action drawables
                actionDrawables.forEachIndexed { index, drawable ->
                    val clickIndex = index + actionIcons.size // Offset for drawables
                    if (clickIndex < onActionClicks.size) {
                        IconButton(onClick = onActionClicks[clickIndex]) {
                            Icon(
                                painter = painterResource(id = drawable),
                                contentDescription = "Action drawable $index"
                            )
                        }
                    }
                }
            }
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Preview
@Composable
fun PreviewCustomTopAppBar() {
    AppTheme {
        CustomTopAppBar(
            title = "Top App Bar",
            navigationIcon = Icons.Filled.Menu,
            onNavigationClick = { /* Handle navigation click */ },
                  )
    }
}



@Preview
@Composable
fun PreviewCustomTopAppBar2() {
    AppTheme {
        CustomTopAppBar(
            title = "Top App Bar",
            navigationIcon = Icons.Filled.Menu,
            actionIcons = listOf(Icons.Filled.Save),
            onActionClicks = listOf { /* Handle action click */ },

        )
    }
}

@Preview
@Composable
fun PreviewCustomTopAppBar3() {
    AppTheme {
        CustomTopAppBar(
            title = "Top App Bar",
            navigationIcon = Icons.Filled.Menu,
            actionDrawables = listOf(R.drawable.save_outlined),
            onActionClicks = listOf { /* Handle action click */ },

        )
    }
}