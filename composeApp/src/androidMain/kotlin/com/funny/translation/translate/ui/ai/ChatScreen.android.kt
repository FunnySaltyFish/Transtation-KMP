package com.funny.translation.translate.ui.ai

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
    ).toString()
}