package com.ganainy.gymmasterscompose.ui.shared_components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.ganainy.gymmasterscompose.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashtagOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    minLines: Int = 1
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

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = keyboardOptions,
        minLines = minLines,
        modifier = modifier
            .fillMaxWidth()
           ,
        placeholder = {
            Text(
                stringResource(R.string.what_s_on_your_mind),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        visualTransformation = hashtagTransformation
    )
}


@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PreviewHashtagOutlinedTextField() {
    HashtagOutlinedTextField(
        value = "normal text #gym #health",
        onValueChange = {}
    )
}
