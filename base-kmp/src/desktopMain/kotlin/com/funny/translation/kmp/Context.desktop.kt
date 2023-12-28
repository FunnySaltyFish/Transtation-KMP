package com.funny.translation.kmp

import androidx.compose.runtime.ProvidableCompositionLocal

actual class KMPContext {
    actual fun getString(id: Int): String {
        TODO("Not yet implemented")
    }

    actual fun getString(id: Int, vararg args: Any?): String {
        TODO("Not yet implemented")
    }
}

actual val LocalKMPContext: ProvidableCompositionLocal<KMPContext>
    get() = TODO("Not yet implemented")

actual val appCtx = KMPContext()