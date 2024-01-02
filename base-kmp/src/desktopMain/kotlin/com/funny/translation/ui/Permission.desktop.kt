package com.funny.translation.ui

import androidx.compose.runtime.Composable

// in desktop, it's empty
@Composable
actual fun Permission(
    permission: String,
    description: String,
    content: @Composable () -> Unit
) {
    content()
}