package com.funny.translation.translate.ui.main

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val LocalWindowSizeState = compositionLocalOf<WindowSizeState> {
    error("LocalWindowSizeState not initialized")
}

/**
 * 窗口大小状态
 */
enum class WindowSizeState {

    /**
     * 垂直状态
     */
    VERTICAL,

    /**
     * 水平状态
     */
    HORIZONTAL;

    val isVertical get() = this == VERTICAL
    val isHorizontal get() = this == HORIZONTAL
}

@Composable
fun ProvideWindowSizeState(
    content: @Composable () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CompositionLocalProvider(
            value = LocalWindowSizeState provides when {
                maxWidth > 720.dp -> WindowSizeState.HORIZONTAL
                else -> WindowSizeState.VERTICAL
            },
            content = content
        )
    }
}