package com.funny.translation.kmp

import androidx.compose.ui.Modifier

// Desktop doesn't support imeNestedScroll
actual fun Modifier.kmpImeNestedScroll(): Modifier = this