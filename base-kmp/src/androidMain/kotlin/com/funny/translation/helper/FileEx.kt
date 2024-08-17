package com.funny.translation.helper

import android.net.Uri
import androidx.core.content.FileProvider
import com.funny.translation.kmp.appCtx
import java.io.File

/**
 * 基于 FileProvider 生成 Uri
 * @receiver File
 * @return Uri
 */
fun File.toAndroidUri(): Uri {
    return FileProvider.getUriForFile(appCtx, "${appCtx.packageName}.fileprovider", this)
}
