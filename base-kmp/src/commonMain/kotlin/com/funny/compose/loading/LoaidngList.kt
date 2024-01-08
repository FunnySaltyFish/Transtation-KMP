package com.funny.compose.loading

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/**
 * 在 LazyList（如 LazyColumn） 中使用加载列表
 * @receiver LazyListScope
 * @param value 加载状态，可以通过 [rememberRetryableLoadingState] 创建
 * @param retry 重试函数，可以通过 [rememberRetryableLoadingState] 创建
 * @param key 用于列表中 items 的参数 key
 * @param loading 加载中显示的页面，默认为三个点
 * @param failure 加载失败显示的页面，默认为文本，点击可以重新加载（retry即为重新加载的函数）
 * @param success 加载成功后 **每一项** 显示的页面，参数 [data] 即为返回的结果
 */
fun <T : Any> LazyListScope.loadingList(
    value: State<LoadingState<List<T>>>,
    retry: () -> Unit,
    key: ((T) -> Any)?,
    loading: @Composable LazyItemScope.() -> Unit = { DefaultLoading() },
    failure: @Composable LazyItemScope.(error: Throwable) -> Unit = {
        DefaultFailure(retry = retry)
    },
    empty: @Composable LazyItemScope.() -> Unit = { },
    successHeader: @Composable (LazyItemScope.() -> Unit)? = null,
    successFooter: @Composable (LazyItemScope.() -> Unit)? = null,
    success: @Composable LazyItemScope.(data: T) -> Unit,
) {
    when (value.value) {
        is LoadingState.Loading -> item(key = "loading") { loading() }
        is LoadingState.Success<*> -> {
            val data = (value.value as LoadingState.Success<List<T>>).data
            if (data.isEmpty()) {
                item(key = "empty") { empty() }
            } else {
                if (successHeader != null) {
                    item(key = "successHeader") {
                        successHeader()
                    }
                }
                items(data, key) {
                    log("loadingList: successfully loaded data: $it")
                    success(it)
                }
                if (successFooter != null) {
                    item(key = "successFooter") {
                        successFooter()
                    }
                }
            }
        }
        is LoadingState.Failure -> item {
            failure(
                (value.value as LoadingState.Failure).error
            )
        }
    }
}

/**
 * 在 LazyGrid（如 LazyVerticalColumn） 中使用加载列表
 * @param value 加载状态，可以通过 [rememberRetryableLoadingState] 创建
 * @param retry 重试函数，可以通过 [rememberRetryableLoadingState] 创建
 * @param key 用于列表中 items 的参数 key
 * @param loading 加载中显示的页面，默认为三个点
 * @param failure 加载失败显示的页面，默认为文本，点击可以重新加载（retry即为重新加载的函数）
 * @param success 加载成功后 **每一项** 显示的页面，参数 [data] 即为返回的结果
 */
fun <T : Any> LazyGridScope.loadingList(
    value: State<LoadingState<List<T>>>,
    retry: () -> Unit,
    key: ((T) -> Any)?,
    span: (LazyGridItemSpanScope.(item: T) -> GridItemSpan)? = null,
    contentType: (item: T) -> Any? = { null },
    loading: @Composable LazyGridItemScope.() -> Unit = { DefaultLoading() },
    failure: @Composable LazyGridItemScope.(error: Throwable) -> Unit = {
        DefaultFailure(retry = retry)
    },
    empty: @Composable LazyGridItemScope.() -> Unit = { },
    successHeader: @Composable (LazyGridItemScope.() -> Unit)? = null,
    successFooter: @Composable (LazyGridItemScope.() -> Unit)? = null,
    success: @Composable LazyGridItemScope.(data: T) -> Unit,
) {
    val fullLineItem = { content: @Composable LazyGridItemScope.() -> Unit ->
        item(span = { GridItemSpan(maxLineSpan) }, content = content)
    }
    when (value.value) {
        is LoadingState.Loading -> fullLineItem { loading() }
        is LoadingState.Success<*> -> {
            val data = (value.value as LoadingState.Success<List<T>>).data
            if (data.isEmpty()) {
                fullLineItem { empty() }
            } else {
                if (successHeader != null) {
                    fullLineItem {
                        successHeader()
                    }
                }
                items(
                    items = data,
                    key = key,
                    span = span,
                    contentType = contentType
                ) {
                    success(it)
                }
                if (successFooter != null) {
                    fullLineItem {
                        successFooter()
                    }
                }
            }
        }
        is LoadingState.Failure -> fullLineItem {
            failure(
                (value.value as LoadingState.Failure).error
            )
        }
    }
}

/**
 * 在 LazyStaggeredGrid（如 LazyVerticalStaggeredGrid） 中使用加载列表
 * @param value 加载状态，可以通过 [rememberRetryableLoadingState] 创建
 * @param retry 重试函数，可以通过 [rememberRetryableLoadingState] 创建
 * @param key 用于列表中 items 的参数 key
 * @param loading 加载中显示的页面，默认为三个点
 * @param failure 加载失败显示的页面，默认为文本，点击可以重新加载（retry即为重新加载的函数）
 * @param success 加载成功后 **每一项** 显示的页面，参数 [data] 即为返回的结果
 */
fun <T : Any> LazyStaggeredGridScope.loadingList(
    value: State<LoadingState<List<T>>>,
    retry: () -> Unit,
    key: ((T) -> Any)?,
    span: ((item: T) -> StaggeredGridItemSpan)? = null,
    contentType: (item: T) -> Any? = { null },
    loading: @Composable LazyStaggeredGridItemScope.() -> Unit = { DefaultLoading() },
    failure: @Composable LazyStaggeredGridItemScope.(error: Throwable) -> Unit = {
        DefaultFailure(retry = retry)
    },
    empty: @Composable LazyStaggeredGridItemScope.() -> Unit = { },
    successHeader: @Composable (LazyStaggeredGridItemScope.() -> Unit)? = null,
    successFooter: @Composable (LazyStaggeredGridItemScope.() -> Unit)? = null,
    success: @Composable LazyStaggeredGridItemScope.(data: T) -> Unit,
) {
    val fullLineItem = { content: @Composable LazyStaggeredGridItemScope.() -> Unit ->
        item(span = StaggeredGridItemSpan.FullLine, content = content)
    }
    when (value.value) {
        is LoadingState.Loading -> fullLineItem { loading() }
        is LoadingState.Success<*> -> {
            val data = (value.value as LoadingState.Success<List<T>>).data
            if (data.isEmpty()) {
                fullLineItem { empty() }
            } else {
                if (successHeader != null) {
                    fullLineItem {
                        successHeader()
                    }
                }
                items(
                    items = data,
                    key = key,
                    span = span,
                    contentType = contentType
                ) {
                    success(it)
                }
                if (successFooter != null) {
                    fullLineItem {
                        successFooter()
                    }
                }
            }
        }
        is LoadingState.Failure -> fullLineItem {
            failure(
                (value.value as LoadingState.Failure).error
            )
        }
    }
}
