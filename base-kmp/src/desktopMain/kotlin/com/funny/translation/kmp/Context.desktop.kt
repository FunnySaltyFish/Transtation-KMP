package com.funny.translation.kmp

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

actual abstract class KMPContext {
    actual fun getString(id: Int): String {
        TODO("Not yet implemented")
    }

    actual fun getString(id: Int, vararg args: Any?): String {
        TODO("Not yet implemented")
    }
}

actual val LocalKMPContext: ProvidableCompositionLocal<KMPContext> =
    staticCompositionLocalOf { appCtx }

actual val appCtx = object : KMPContext() {

}