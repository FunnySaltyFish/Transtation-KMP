package com.funny.translation.translate.utils

import com.funny.translation.helper.CacheManager
import java.io.File

actual val UpdateUtils.downloadedFile: File by lazy {
    CacheManager.cacheDir.resolve("exe/update_exe.exe")
}