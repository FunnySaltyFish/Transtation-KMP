package com.funny.translation.translate.utils

import kotlinx.coroutines.CoroutineScope

actual object InitUtil {
    actual suspend fun CoroutineScope.initAndroidActivity() {

    }

    actual suspend fun CoroutineScope.initDesktopActivity() {}
}