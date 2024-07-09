package com.funny.translation.translate.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// 做某项任务的 button，点击后前面加上圈圈，并且不可点击，直至任务完成或者失败
@Composable
fun TaskButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    loading: Boolean = false,
    enabled: Boolean = true,
    loadingColor: Color = MaterialTheme.colorScheme.primary,
    loadingContent: @Composable () -> Unit = {
        CircularProgressIndicator(
            color = loadingColor,
            strokeWidth = 2.dp,
            modifier = Modifier.size(16.dp)
        )
    },
    content: @Composable () -> Unit
) {
    val loadingModifier = Modifier
        .clickable(enabled = enabled, onClick = onClick)
        .animateContentSize()
        .then(modifier)

    Button(
        onClick = onClick,
        enabled = enabled and !loading,
        modifier = loadingModifier
    ) {
        AnimatedVisibility(visible = loading) {
            loadingContent()
        }
        content()
    }
}
