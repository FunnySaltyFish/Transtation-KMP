package com.funny.translation.translate.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.funny.translation.translate.ui.widget.ExpandMoreButton

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.expandableStickyRow(
    expand: Boolean,
    updateExpand: (Boolean) -> Unit,
    headlineContent: @Composable () -> Unit,
    supportContent: (@Composable () -> Unit)? = null,
    contentList: LazyListScope.() -> Unit
) {
    stickyHeader {
        ListItem(
            headlineContent = headlineContent,
            modifier = Modifier
                .clickable { updateExpand(!expand) },
            supportingContent = supportContent,
            trailingContent = {
                ExpandMoreButton(expand = expand, onClick = {
                    updateExpand(!expand)
                })
            }
        )
    }

    if (expand) contentList()
}

fun LazyListScope.expandableStickyRow(
    title: String,
    expand: Boolean,
    updateExpand: (Boolean) -> Unit,
    contentList: LazyListScope.() -> Unit
) {
    expandableStickyRow(
        expand = expand,
        updateExpand = updateExpand,
        headlineContent = {
            Text(
                text = title,
                fontWeight = FontWeight.W600,
                fontSize = 24.sp
            )
        },
        contentList = contentList
    )
}