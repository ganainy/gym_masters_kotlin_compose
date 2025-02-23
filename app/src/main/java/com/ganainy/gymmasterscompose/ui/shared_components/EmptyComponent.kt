package com.ganainy.gymmasterscompose.ui.shared_components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.R


/**
 * A composable function to display an error message with an icon.
 *
 * @param error the error message.
 * @param modifier the modifier to use for the error component.
 * @param onRetryClick an optional lambda to invoke when the retry button is clicked.
 */
@Composable
fun EmptyComponent(
    text: String,
    modifier: Modifier = Modifier,
    onRetryClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(128.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))

        onRetryClick?.let {
            GhostButton(
                onClick = it,
                text = stringResource(R.string.retry)
            )
        }
    }
}

/**
 * A preview function for the [ErrorComponent] with a vector icon.
 */
@Preview(showBackground = true)
@Composable
fun EmptyComponentPreviewWithVector() {
    MaterialTheme {
        EmptyComponent(
            text = "Something went wrong!",
            onRetryClick = {}
        )
    }
}
