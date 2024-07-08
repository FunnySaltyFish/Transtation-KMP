package com.funny.translation.translate.ui.ai

import com.eygraber.uri.toUri
import com.funny.translation.helper.CacheManager
import com.funny.translation.helper.now

actual fun getPhotoUri(): String {
    val file: java.io.File = CacheManager.cacheDir.resolve("photo_${now()}.jpg")
    return file.toUri().toString()
}