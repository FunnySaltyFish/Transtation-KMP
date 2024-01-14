package com.funny.translation.helper

import kotlinx.coroutines.flow.MutableStateFlow

actual object ApplicationUtil {
    var restartAppFlag = MutableStateFlow(0)

    actual fun restartApp() {
        // restart desktop app
        // TODO
        restartAppFlag.value++
    }
}