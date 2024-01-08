package com.funny.compose.loading

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


/**
 * 通用加载微件
 * @author [FunnySaltyFish](https://github.com/FunnySaltyFish)
 * @param modifier Modifier 整个微件包围在Box中，此处修饰此Box
 * @param loader  加载函数，返回值为正常加载出的结果
 * @param loading 加载中显示的页面，默认为三个点的Loading
 * @param failure 加载失败显示的页面，默认为文本，点击可以重新加载（retry即为重新加载的函数）
 * @param success 加载成功后的页面，参数 [data] 即为返回的结果
 */
@Composable
fun <T, K> LoadingContent(
    modifier: Modifier = Modifier,
    retryKey: K,
    updateRetryKey: (K) -> Unit,
    loader: suspend () -> T,
    loading: @Composable BoxScope.() -> Unit = { DefaultLoading() },
    failure: @Composable BoxScope.(error: Throwable, retry: () -> Unit) -> Unit = { error, retry ->
        DefaultFailure(retry = retry)
    },
    success: @Composable BoxScope.(data: T) -> Unit
) {
    val state: LoadingState<T> by rememberRetryableLoadingState(
        loader = loader,
        retryKey = retryKey
    )
    Box(modifier = modifier) {
        when (state) {
            is LoadingState.Loading -> loading()
            is LoadingState.Success<T> -> success((state as LoadingState.Success<T>).data)
            is LoadingState.Failure -> failure((state as LoadingState.Failure).error) {
                updateRetryKey(retryKey)
            }
        }
    }
}

/**
 * 通用加载微件
 * @param modifier Modifier 整个微件包围在Box中，此处修饰此Box
 * @param initialValue LoadingState<T> 初始加载值
 * @param retry 重试函数
 * @param loader 加载函数，返回值为正常加载出的结果
 * @param loading 加载时显示的页面，默认为三个点
 * @param failure 加载失败显示的页面，默认为文本，点击可以重新加载（retry即为重新加载的函数）
 * @param success 加载成功后的页面，参数 [data] 即为返回的结果
 */
@Composable
fun <T> LoadingContent(
    modifier: Modifier = Modifier,
    initialValue: LoadingState<T>,
    retry: () -> Unit,
    loader: suspend () -> T,
    loading: @Composable BoxScope.() -> Unit = { DefaultLoading() },
    failure: @Composable BoxScope.(error: Throwable, retry: () -> Unit) -> Unit = { error, _ ->
        DefaultFailure(retry = retry)
    },
    success: @Composable BoxScope.(data: T) -> Unit
) {
    val state by rememberRetryableLoadingState(
        loader = loader, initialValue = initialValue, retryKey = initialValue
    )
    Box(modifier = modifier) {
        when (state) {
            is LoadingState.Loading -> loading()
            is LoadingState.Success<T> -> success((state as LoadingState.Success<T>).data)
            is LoadingState.Failure -> failure((state as LoadingState.Failure).error) {
                retry()
            }
        }
    }
}

/**
 * 通用加载微件
 * @param modifier Modifier 整个微件包围在Box中，此处修饰此Box
 * @param loader 加载函数，返回值为正常加载出的结果
 * @param loading 加载时显示的页面，默认为三个点
 * @param failure 加载失败显示的页面，默认为文本，点击可以重新加载（retry即为重新加载的函数）
 * @param success 加载成功后的页面，参数 [data] 即为返回的结果
 */
@Composable
fun <T> LoadingContent(
    modifier: Modifier = Modifier,
    loader: suspend () -> T,
    loading: @Composable BoxScope.() -> Unit = { DefaultLoading() },
    failure: @Composable BoxScope.(error: Throwable, retry: () -> Unit) -> Unit = { error, retry ->
        DefaultFailure(retry = retry)
    },
    success: @Composable BoxScope.(data: T) -> Unit
) {
    var key by remember {
        mutableStateOf(false)
    }
    LoadingContent(modifier, key, { k -> key = !k }, loader, loading, failure, success)
}



/**
 * 可重试的 LoadingState，由于 Key 内部持有，额外返回一个修改 Key 的函数
 * @param initialValue LoadingState<T> 初始加载状态，默认为 [LoadingState.Loading]
 * @param loader 加载值的函数
 * @return Pair<State<LoadingState<T>>, () -> Unit>
 */
@Composable
fun <T> rememberRetryableLoadingState(
    initialValue: LoadingState<T> = LoadingState.Loading,
    loader: suspend () -> T,
): Pair<State<LoadingState<T>>, () -> Unit> {
    var retryKey by remember {
        mutableStateOf(false)
    }
    val update = remember {
        {
            retryKey = !retryKey
        }
    }
    val loadingState: State<LoadingState<T>> =
        rememberRetryableLoadingState(
            retryKey = retryKey,
            loader = loader,
            initialValue = initialValue
        )
    return (loadingState to update)
}

/**
 * 可重试的 LoadingState
 * @param initialValue LoadingState<T> 初始加载状态，默认为 [LoadingState.Loading]
 * @param retryKey K 用于重试的 Key，当 Key 发生变化时，会重新加载
 * @param loader 加载值的函数
 * @return State<LoadingState<T>>
 */
@Composable
fun <T, K> rememberRetryableLoadingState(
    initialValue: LoadingState<T> = LoadingState.Loading,
    retryKey: K,
    loader: suspend () -> T,
): State<LoadingState<T>> {
    val loadingState: MutableState<LoadingState<T>> = remember {
        mutableStateOf(initialValue)
    }
    LaunchedEffect(retryKey) {
        if (!initialValue.isSuccess) {
            loadingState.value = LoadingState.Loading
            loadingState.value = try {
                LoadingState.Success(loader())
            } catch (e: Exception) {
                e.printStackTrace()
                LoadingState.Failure(e)
            }
        }
    }
    return loadingState
}