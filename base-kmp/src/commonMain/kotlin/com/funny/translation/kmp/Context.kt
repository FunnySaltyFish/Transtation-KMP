package com.funny.translation.kmp

import androidx.compose.runtime.ProvidableCompositionLocal

expect class KMPContext {
    fun getString(id: Int): String
    fun getString(id: Int, vararg args: Any?): String
}

expect val LocalKMPContext: ProvidableCompositionLocal<KMPContext>

expect val appCtx: KMPContext