package com.funny.translation.ui

import androidx.compose.runtime.Composable

@Composable
expect fun Permission(
    permission: String,
    description: String,
    content: @Composable () -> Unit = { }
)