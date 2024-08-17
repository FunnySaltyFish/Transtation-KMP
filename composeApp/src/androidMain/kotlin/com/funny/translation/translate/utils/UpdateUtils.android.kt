package com.funny.translation.translate.utils

import com.funny.translation.helper.CacheManager
import java.io.File

private const val REQUEST_INSTALL_APK_CODE = 100

actual val UpdateUtils.downloadedFile: File by lazy {
    CacheManager.cacheDir.resolve("apk/update_apk.apk")
}