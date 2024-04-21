package com.funny.translation.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier

/**
 * WindowInsets.safeDrawing, Exclude ime
 */
val WindowInsets.Companion.safeMain: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)

@Composable
fun Modifier.safeMainPadding() = windowInsetsPadding(WindowInsets.safeMain)