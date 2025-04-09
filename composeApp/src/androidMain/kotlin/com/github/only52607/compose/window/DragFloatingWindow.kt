package com.github.only52607.compose.window

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import com.funny.translation.AppConfig

fun Modifier.dragFloatingWindow(): Modifier = composed {
    val floatingWindow = LocalFloatingWindow.current
    val windowParams = remember { floatingWindow.windowParams }
    pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consume()
            val w = floatingWindow.decorView.width
            val h = floatingWindow.decorView.height
            windowParams.x =
                (windowParams.x + dragAmount.x.toInt()).coerceIn(0..(AppConfig.SCREEN_WIDTH - w))
            windowParams.y =
                (windowParams.y + dragAmount.y.toInt()).coerceIn(0..(AppConfig.SCREEN_HEIGHT - h))
            floatingWindow.update()
        }
    }
}