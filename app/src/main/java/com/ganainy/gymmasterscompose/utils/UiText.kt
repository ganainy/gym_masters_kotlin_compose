package com.ganainy.gymmasterscompose.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

//sealed class to represent different types of text to avoid using context in viewModels
sealed class UiText {
    data class String(val value: kotlin.String) : UiText()
    class StringResource(@StringRes val resId: Int, vararg val args: Any) : UiText()

    @Composable
    fun asString(): kotlin.String {
        return when (this) {
            is String -> value
            is StringResource -> stringResource(id = resId, formatArgs = args)
        }
    }
}
