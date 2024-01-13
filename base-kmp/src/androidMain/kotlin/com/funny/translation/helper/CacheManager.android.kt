package com.funny.translation.helper

import com.funny.translation.kmp.appCtx
import java.io.File

actual object CacheManager {
    private val ctx get() = appCtx

    actual var cacheDir: File = ctx.externalCacheDir ?: ctx.cacheDir

    val innerCacheDir: File = ctx.cacheDir
}