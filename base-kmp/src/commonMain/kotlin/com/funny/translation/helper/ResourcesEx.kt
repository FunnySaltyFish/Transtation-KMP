package com.funny.translation.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.funny.translation.kmp.LocalKMPContext
import com.funny.translation.kmp.appCtx

private const val PREFIX_ASSETS_KEY = "__assets__"
private const val TAG = "ResourcesEx"

fun string(id: Int) = appCtx.getString(id)
fun string(id: Int, vararg args: Any) = appCtx.getString(id, *args)

@Composable
@ReadOnlyComposable
fun assetsString(name: String): String {
    val context = LocalKMPContext.current
    val assetsKey = PREFIX_ASSETS_KEY + name
    val readData = DataHolder.get<String?>(assetsKey)
    return if (readData != null) {
        readData
    } else {
        val readAssets = context.readAssets(name)
        DataHolder.put(assetsKey, readAssets)
        readAssets
    }
}

/**
 * 带 i18n 的 assetsString，文件名应当形如 xxx_zh.json
 * 如果找不到带语言的文件，则使用默认的文件
 * @param name String
 * @return String
 */
@Composable
@ReadOnlyComposable
expect fun assetsStringLocalized(name: String): String

internal fun String.addBeforeFileEx(text: String): String {
    val index = lastIndexOf(".")
    return if (index > 0) {
        substring(0, index) + text + substring(index)
    } else {
        this
    }
}