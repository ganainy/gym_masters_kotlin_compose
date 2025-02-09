package com.ganainy.gymmasterscompose.ui.theme.repository


sealed class ResultWrapper<out T> {
    data class Success<T>(val data: T) : ResultWrapper<T>()
    data class Error(val exception: Exception) : ResultWrapper<Nothing>()
}

fun <T> ResultWrapper<T>?.orEmpty(defaultValue: T): T {
    return (this as? ResultWrapper.Success<T>)?.data ?: defaultValue
}

fun <T> ResultWrapper<T>.onSuccess(action: (T) -> Unit): ResultWrapper<T> {
    if (this is ResultWrapper.Success<T>) action(data)
    return this
}

fun ResultWrapper<*>.onError(action: (Exception) -> Unit): ResultWrapper<*> {
    if (this is ResultWrapper.Error) action(exception)
    return this
}