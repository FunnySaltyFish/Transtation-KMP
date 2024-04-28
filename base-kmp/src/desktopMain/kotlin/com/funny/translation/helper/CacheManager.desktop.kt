package com.funny.translation.helper

import ca.gosyer.appdirs.AppDirs
import java.io.File

actual object CacheManager {
    private val appName = "Transtation"
    private val author = "FunnySaltyFish"
    private val appDir = AppDirs(appName, author)
    private val userHome = appDir.getUserDataDir()
    val baseDir = File(userHome)

    actual var cacheDir: File = baseDir.resolve("cache")

    init {
        Log.d("CacheManager", "baseDir=$baseDir")
    }
}