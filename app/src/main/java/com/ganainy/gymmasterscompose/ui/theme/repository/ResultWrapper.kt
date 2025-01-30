package com.ganainy.gymmasterscompose.ui.theme.repository


sealed class ResultWrapper<out T> {
    data class Success<T>(val data: T) : ResultWrapper<T>()
    data class Error(val exception: Exception) : ResultWrapper<Nothing>()
}

fun <T> ResultWrapper<T>?.orEmpty(defaultValue: T): T {
    return (this as? ResultWrapper.Success<T>)?.data ?: defaultValue
}
