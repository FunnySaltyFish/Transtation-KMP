package com.funny.translation.translate.utils

import com.funny.translation.helper.CacheManager
import com.funny.translation.translate.bean.UpdateInfo
import java.io.File

actual fun getInstallApkFile(updateInfo: UpdateInfo): File? {
    val versionName = updateInfo.version_name ?: return null
    val versionCode = updateInfo.version_code ?: return null
    val fileSuffix = updateInfo.file_extension ?: "apk"
    return CacheManager.cacheDir.resolve("update/${versionName}_$versionCode.$fileSuffix")
}

actual val allowCheckUpdate: Boolean = true