package com.funny.translation.kmp

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.WindowState

actual open class KMPActivity: KMPContext() {
    lateinit var windowState: WindowState
    val windowShowState = mutableStateOf(false)
//    lateinit var onBack: (DataType) -> Unit
}
