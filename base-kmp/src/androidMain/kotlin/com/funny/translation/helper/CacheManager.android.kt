package com.funny.translation.helper

import com.funny.translation.BaseApplication
import java.io.File

actual object CacheManager {
    private val ctx = BaseApplication.ctx

    actual var cacheDir: File = ctx.externalCacheDir ?: ctx.cacheDir
}