@file:OptIn(ExperimentalResourceApi::class)

package com.funny.translation.kmp

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource
import java.io.ByteArrayInputStream
import java.io.InputStream


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

actual fun KMPContext.openAssetsFile(fileName: String): InputStream {
    return runBlocking {
        ByteArrayInputStream(resource("assets/$fileName").readBytes())
    }
}

actual fun KMPContext.readAssetsFile(fileName: String): String {
    return runBlocking {
        resource("assets/$fileName").readBytes().toString()
    }
}