package com.funny.translation.helper

import java.io.File

actual object CacheManager {
    private val appName = "Transtation"
    private val userHome = System.getProperty("user.home")
    val baseDir  = File(userHome, appName)

    actual var cacheDir: File = baseDir.resolve("cache")

    var configDir = baseDir.resolve("config")
}