package com.funny.translation.ui

import androidx.compose.runtime.Composable

@Composable
expect fun SystemBarSettings(
    hideStatusBar: Boolean = false,
)