// File: AnyPopDialogState.kt
package com.funny.translation.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Stable
class AnyPopDialogState(initialVisible: Boolean = false) {
    var isVisible by mutableStateOf(initialVisible)
        private set

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }

    fun toggle() {
        isVisible = !isVisible
    }

    fun animateShow() = show()
    fun animateHide() = hide()

    companion object {
        val Saver = listSaver(
            save = { listOf(it.isVisible) },
            restore = { AnyPopDialogState(it[0]) }
        )
    }
}

@Composable
fun rememberAnyPopDialogState(initialVisible: Boolean = false): AnyPopDialogState {
    return rememberSaveable(saver = AnyPopDialogState.Saver) { AnyPopDialogState(initialVisible) }
}