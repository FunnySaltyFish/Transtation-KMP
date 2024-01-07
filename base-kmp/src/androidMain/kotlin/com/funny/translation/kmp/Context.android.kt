@file:OptIn(ExperimentalResourceApi::class)

package com.funny.translation.kmp

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.funny.translation.BaseApplication
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource
import java.io.ByteArrayInputStream
import java.io.InputStream

actual typealias KMPContext = Context

actual val LocalKMPContext = LocalContext

actual val appCtx: KMPContext
    get() = BaseApplication.ctx

actual fun KMPContext.openAssetsFile(fileName: String): InputStream {
    return runBlocking {
        ByteArrayInputStream(resource(fileName).readBytes())
    }
}

actual fun KMPContext.readAssetsFile(fileName: String): String {
    return runBlocking {
        resource(fileName).readBytes().toString()
    }
}