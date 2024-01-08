package com.funny.translation.kmp.paging

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable

fun <T: Any> LazyListScope.items(
    items: LazyPagingItems<T>,
    key: ( (T) -> Any )? = null,
    contentType: ( (T) -> Any )? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    items(
        items.itemCount,
        key = items.itemKey(key),
        contentType = items.itemContentType(contentType)
    ) loop@ { i ->
        val item = items[i] ?: return@loop
        itemContent(item)
    }
}