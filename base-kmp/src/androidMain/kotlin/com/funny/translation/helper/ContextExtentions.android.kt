package com.funny.translation.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.funny.translation.helper.handler.runOnUI
import com.funny.translation.kmp.base.strings.ResStrings
import com.funny.translation.kmp.readAssetsFile
import com.hjq.toast.Toaster

actual fun Context.readAssets(fileName: String): String = readAssetsFile(fileName)

fun Context.openUrl(url: String) {
    openUrl(Uri.parse(url))
}

fun Context.openUrl(uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = uri

    try {
        startActivity(intent)
    } catch (e: Exception) {
        try {
            startActivity(Intent.createChooser(intent, ResStrings.please_choose_browser))
        } catch (e: Exception) {
            toastOnUi(e.localizedMessage ?: "open url error")
        }
    }
}


actual inline fun Context.toastOnUi(message: Int, length: Int) {
    toastOnUi(getString(message), length)
}

// 内联以使得框架能够获取到调用的真正位置
actual inline fun Context.toastOnUi(message: CharSequence?, length: Int) {
    runOnUI {
        if (length == Toast.LENGTH_SHORT) {
            Toaster.showShort(message)
        } else {
            Toaster.showLong(message)
        }
    }
}
