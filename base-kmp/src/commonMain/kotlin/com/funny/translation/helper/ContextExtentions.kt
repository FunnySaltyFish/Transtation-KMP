package com.funny.translation.helper

import com.funny.translation.kmp.KMPContext
import com.funny.translation.kmp.LocalKMPContext
import com.funny.translation.kmp.appCtx
import com.funny.translation.ui.toast.ToastUIState
import java.io.File

typealias Context = KMPContext
val LocalContext = LocalKMPContext

private const val TAG = "ContextExtensions"

val toastState = ToastUIState()

expect fun Context.readAssets(fileName: String): String

// 0 -> Toast.LENGTH_SHORT
expect inline fun Context.toastOnUi(message: Int, length: Int = 0)

// 内联以使得框架能够获取到调用的真正位置
expect inline fun KMPContext.toastOnUi(message: CharSequence?, length: Int = 0)

expect fun KMPContext.openUrl(url: String)

val Context.externalCache: File
    get() = CacheManager.cacheDir

inline fun toast(message: CharSequence?, length: Int = 0) = appCtx.toastOnUi(message, length)