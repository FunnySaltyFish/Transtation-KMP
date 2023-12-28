package com.funny.translation.helper

import java.io.File

actual object CacheManager {
    private val appName = "Transtation"
    private val userHome = System.getProperty("user.home")
    private val baseDir  = File(userHome, appName)

    actual var cacheDir: File = baseDir.resolve("cache")
}