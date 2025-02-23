package com.ganainy.gymmasterscompose.ui.screens.create_post.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ganainy.gymmasterscompose.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashtagMultiLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = stringResource(R.string.what_s_on_your_mind),
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Done
    ),
    minLines: Int = 5,
    maxLines: Int = 10,
    onHashtagClick: (String) -> Unit = {},
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null
) {
    // Colors and styling
    val hashtagColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val containerColor = MaterialTheme.colorScheme.surface
    val errorColor = MaterialTheme.colorScheme.error

    // State for focus and interaction
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Hashtag transformation
    val hashtagTransformation = remember(hashtagColor) {
        VisualTransformation { text ->
            val annotatedString = buildAnnotatedString {
                val words = text.text.split("\\s+".toRegex())
                words.forEachIndexed { index, word ->
                    if (word.startsWith("#") && word.length > 1 && word.matches(Regex("#[\\w-]+"))) {
                        pushStringAnnotation(tag = "hashtag", annotation = word)
                        withStyle(
                            SpanStyle(
                                color = hashtagColor,
                                fontWeight = FontWeight.Medium,
                                textDecoration = TextDecoration.None
                            )
                        ) {
                            append(word)
                        }
                        pop()
                    } else {
                        withStyle(SpanStyle(color = textColor)) {
                            append(word)
                        }
                    }
                    if (index < words.size - 1) append(" ")
                }
            }
            TransformedText(annotatedString, OffsetMapping.Identity)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        TextField(
            value = value,
            onValueChange = { newValue ->
                val processedValue = if (newValue.lastOrNull() == '#' && newValue.length > value.length) {
                    "$newValue "
                } else {
                    newValue
                }
                onValueChange(processedValue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusable(interactionSource = interactionSource),
            enabled = enabled,
            isError = isError,
            label = if (label.isNotEmpty()) {
                { Text(label, style = MaterialTheme.typography.labelLarge) }
            } else null,
            placeholder = {
                Text(
                    text = placeholder,
                    color = placeholderColor,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            supportingText = supportingText?.let {
                {
                    Text(
                        text = it,
                        color = if (isError) errorColor else textColor.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = textColor,
                lineHeight = 24.sp
            ),
            visualTransformation = hashtagTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            minLines = minLines,
            maxLines = maxLines,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                disabledContainerColor = containerColor.copy(alpha = 0.5f),
                errorContainerColor = containerColor,
                // Remove all indicator lines
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = hashtagColor
            )
        )

        // Hashtag suggestions placeholder
        if (isFocused && value.lastOrNull() == '#') {
            Text(
                text = "Start typing a hashtag for suggestions...",
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, start = 16.dp)
            )
        }
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PreviewHashtagOutlinedTextField() {
    HashtagMultiLineTextField(
        value = "normal text #gym #health",
        onValueChange = {}
    )
}
