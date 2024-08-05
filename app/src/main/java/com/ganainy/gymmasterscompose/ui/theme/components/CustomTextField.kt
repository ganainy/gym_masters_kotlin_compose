package com.ganainy.gymmasterscompose.ui.theme.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp


@Composable
fun CustomTextField(
    text: String, label: String,
    options: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false, errorText: String = "",
    onValueChange: (String) -> Unit
) {
    //Flag to not show error on app start and only after interacting with the field
    var isFieldInteractedWith by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = text,
        onValueChange = { onValueChange(it) },
        label = { Text(label) },
        keyboardOptions = options,
        isError = isError && isFieldInteractedWith,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    isFieldInteractedWith = true
                }
            },
        supportingText = {
            if (isError && isFieldInteractedWith) {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}