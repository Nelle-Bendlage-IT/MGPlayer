package com.mgtv.shared_core.core

sealed class ViewState<out T> {
    data object Loading : ViewState<Nothing>()

    data object Empty : ViewState<Nothing>()

    data class Success<out T>(
        val data: T,
    ) : ViewState<T>()

    data class Error(val message: String) : ViewState<Nothing>()
}
