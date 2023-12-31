package com.funny.translation.helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

val scope = CoroutineScope(EmptyCoroutineContext)

actual fun Context.readAssets(fileName: String): String  {
    return ""
}

// 0 -> Toast.LENGTH_SHORT
actual inline fun Context.toastOnUi(message: Int, length: Int) {

}

// 内联以使得框架能够获取到调用的真正位置
actual inline fun Context.toastOnUi(message: CharSequence?, length: Int) {
    scope.launch {
        toastState.show(message.toString())
    }
}