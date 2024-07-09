package com.funny.translation.translate.ui.ai

import android.content.Intent
import androidx.core.content.FileProvider
import com.funny.translation.helper.CacheManager
import com.funny.translation.helper.now
import com.funny.translation.kmp.appCtx
import com.funny.translation.translate.utils.createParentDirIfNotExist

actual fun getPhotoUri(): String {
    val file: java.io.File = CacheManager.cacheDir.resolve("photos/photo_${now()}.jpg")
    file.createParentDirIfNotExist()
    return FileProvider.getUriForFile(
        appCtx, "${appCtx.packageName}.fileprovider", file
    ).also {
        // 授权
        appCtx.grantUriPermission(
            appCtx.packageName,
            it,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }.toString()
}