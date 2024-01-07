package com.funny.translation.kmp

import androidx.compose.runtime.ProvidableCompositionLocal
import java.io.InputStream

expect abstract class KMPContext {
    fun getString(id: Int): String
    fun getString(id: Int, vararg args: Any?): String
}

expect fun KMPContext.openAssetsFile(fileName: String): InputStream

expect fun KMPContext.readAssetsFile(fileName: String): String

expect val LocalKMPContext: ProvidableCompositionLocal<KMPContext>

expect val appCtx: KMPContext