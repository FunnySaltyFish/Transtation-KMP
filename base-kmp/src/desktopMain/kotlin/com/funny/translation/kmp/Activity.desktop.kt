package com.funny.translation.kmp

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.WindowState

actual class KMPActivity(val state: WindowState) : KMPContext() {
    val windowShowState = mutableStateOf(false)
    lateinit var onBack: (DataType) -> Unit
}
