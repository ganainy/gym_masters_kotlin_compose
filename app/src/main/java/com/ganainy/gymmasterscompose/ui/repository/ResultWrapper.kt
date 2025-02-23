package com.ganainy.gymmasterscompose.ui.repository


sealed class ResultWrapper<out T>(open val isLoading: Boolean = false) {
    data class Success<T>(val data: T, override val isLoading: Boolean = false) : ResultWrapper<T>(isLoading)
    data class Loading(override val isLoading: Boolean = true) : ResultWrapper<Nothing>(isLoading)
    data class Error(val exception: Exception, override val isLoading: Boolean = false) : ResultWrapper<Nothing>(isLoading)
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