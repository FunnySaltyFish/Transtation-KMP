@file:OptIn(ExperimentalResourceApi::class)

package com.funny.translation.kmp

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.funny.translation.BaseApplication
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes
import java.io.ByteArrayInputStream
import java.io.InputStream

actual typealias KMPContext = Context

actual val LocalKMPContext = LocalContext

actual val appCtx: KMPContext
    get() = BaseApplication.ctx

@OptIn(InternalResourceApi::class)
actual fun KMPContext.openAssetsFile(fileName: String): InputStream {
    return runBlocking {
        ByteArrayInputStream(readResourceBytes("assets/$fileName"))
    }
}

actual fun KMPContext.readAssetsFile(fileName: String): String {
    return openAssetsFile(fileName).bufferedReader().use { it.readText() }
}