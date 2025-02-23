package com.ganainy.gymmasterscompose.ui.shared_components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * A composable function that displays text with clickable hashtags.
 *
 * This function creates a text element where hashtags (words starting with #) are highlighted
 * and clickable. When a hashtag is clicked, the provided callback function is invoked with the
 * hashtag text.
 *
 * @param text The text to be displayed, which may contain hashtags.
 * @param onHashtagClick A callback function to be invoked when a hashtag is clicked.
 * @param modifier A [Modifier] for this text element.
 */
@Composable
fun HashtagText(
    text: String,
    onHashtagClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    val hashtagColor = MaterialTheme.colorScheme.primary

    val annotatedString = buildAnnotatedString {
        val regex = Regex("#\\w+") // Match hashtags
        var lastIndex = 0

        regex.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1

            // Append text before hashtag
            append(text.substring(lastIndex, start))

            // Add clickable hashtag
            pushStringAnnotation(tag = "HASHTAG", annotation = matchResult.value)
            withStyle(SpanStyle(color = hashtagColor, textDecoration = TextDecoration.Underline)) {
                append(matchResult.value)
            }
            pop()

            lastIndex = end
        }

        // Append remaining text
        append(text.substring(lastIndex))
    }

    Text(
        text = annotatedString,
        style = style,
        modifier = modifier
            .padding(vertical = 4.dp)
            .clickable {
                // Handle hashtag clicks
                annotatedString.getStringAnnotations(tag = "HASHTAG", start = 0, end = text.length)
                    .firstOrNull()?.let { annotation ->
                        onHashtagClick(annotation.item)
                    }
            }
    )
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun HashtagTextPreview() {
    HashtagText(
        text = "Hello #world #gymmasters", onHashtagClick = {}, style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    )
}
