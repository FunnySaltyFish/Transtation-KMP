package com.funny.translation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.WindowState
import com.funny.translation.helper.Log
import com.funny.translation.kmp.DataType
import com.funny.translation.kmp.KMPActivity

actual open class BaseActivity : KMPActivity() {
    lateinit var windowState: WindowState
    val windowShowState = mutableStateOf(false)
    var data: DataType? = null
//    lateinit var onBack: (DataType) -> Unit

    open fun onShow() {
        Log.d("BaseActivity", "onShow: $this")
    }

    open fun onStart() {
        Log.d("BaseActivity", "onStart: $this")
    }

    fun finish() {
        windowShowState.value = false
    }

    override fun toString(): String {
        return "Activity: ${this::class.simpleName}, show = ${windowShowState.value}"
    }
}