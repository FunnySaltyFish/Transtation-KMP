package com.funny.translation.translate.utils

import com.funny.translation.translate.bean.UpdateInfo
import java.io.File

// Google Play Store 不允许应用内更新

actual val allowCheckUpdate: Boolean = true
actual fun getInstallApkFile(updateInfo: UpdateInfo): File? = null