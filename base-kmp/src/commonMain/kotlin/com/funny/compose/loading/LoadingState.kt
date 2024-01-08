package com.funny.compose.loading

/**
 * A sealed class to represent the state of loading.
 *
 * R: The type of data that will be loaded.
 * @property isLoading Boolean Whether the loading is in progress.
 * @property isSuccess Boolean Whether the loading is successful.
 */
sealed class LoadingState<out R> {
    object Loading : LoadingState<Nothing>()
    data class Failure(val error : Throwable) : LoadingState<Nothing>()
    data class Success<T>(val data : T) : LoadingState<T>()

    val isLoading
        get() = this is Loading
    val isSuccess
        get() = this is Success<*>
}