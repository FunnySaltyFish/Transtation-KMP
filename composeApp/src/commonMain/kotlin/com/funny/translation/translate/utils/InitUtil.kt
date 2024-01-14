package com.funny.translation.translate.utils

import com.funny.translation.debug.Debug
import com.funny.translation.debug.DefaultDebugTarget
import com.funny.translation.sign.SignUtils
import com.funny.translation.translate.initLanguageDisplay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

expect object InitUtil {
    suspend fun CoroutineScope.initAndroidActivity()
    suspend fun CoroutineScope.initDesktopActivity()
}


internal suspend fun InitUtil.initCommon() {
    Debug.addTarget(DefaultDebugTarget)

    CoroutineScope(Dispatchers.IO).launch {
        SignUtils.loadJs()
        SortResultUtils.init()
    }

    initTypeConverters()
    initLanguageDisplay()
}