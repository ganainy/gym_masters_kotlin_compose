package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganainy.gymmasterscompose.R


/**
 * A composable function that displays a text field with hashtag highlighting.
 *
 * This function creates a text field that highlights words starting with a hashtag (#) in a different color.
 * It uses a custom visual transformation to apply the highlighting.
 *
 * @param value The current text to be displayed in the text field.
 * @param onValueChange A callback function to be invoked when the text changes.
 * @param modifier A [Modifier] for this text field.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashtagTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hashtagColor = MaterialTheme.colorScheme.primary

    val hashtagTransformation = VisualTransformation { text ->
        val annotatedString = buildAnnotatedString {
            val words = text.text.split("\\s+".toRegex())
            words.forEachIndexed { index, word ->
                if (word.startsWith("#") && word.length > 1) {
                    withStyle(SpanStyle(color = hashtagColor, textDecoration = TextDecoration.None)) {
                        append(word)
                    }
                } else {
                    append(word)
                }
                if (index < words.size - 1) append(" ")
            }
        }
        TransformedText(annotatedString, OffsetMapping.Identity)
    }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = {
            Text(
                stringResource(R.string.what_s_on_your_mind),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
        visualTransformation = hashtagTransformation // Apply transformation here
    )
}


@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PreviewHashtagTextField() {
    HashtagTextField(
        value = "normal text #gym #health",
        onValueChange = {}
    )
}
