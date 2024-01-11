package com.funny.translation.helper

import com.funny.translation.kmp.KMPContext
import com.funny.translation.kmp.base.strings.ResStrings
import com.funny.translation.kmp.readAssetsFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI
import kotlin.coroutines.EmptyCoroutineContext

val scope = CoroutineScope(EmptyCoroutineContext)

actual fun Context.readAssets(fileName: String): String  = readAssetsFile(fileName)

// 0 -> Toast.LENGTH_SHORT
actual inline fun Context.toastOnUi(message: Int, length: Int) {

}

// 内联以使得框架能够获取到调用的真正位置
actual inline fun Context.toastOnUi(message: CharSequence?, length: Int) {
    scope.launch {
        toastState.show(message.toString())
    }
}

actual fun KMPContext.openUrl(url: String) {
    // 调用浏览器打开此 url
    try {
        Desktop.getDesktop().browse(URI(url))
    } catch (e: Exception) {
        toastOnUi(ResStrings.failed_unknown_err)
        // 处理异常
        e.printStackTrace()
    }
}