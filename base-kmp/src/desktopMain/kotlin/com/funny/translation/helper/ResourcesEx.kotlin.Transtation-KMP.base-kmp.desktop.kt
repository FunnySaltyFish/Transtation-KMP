package com.funny.translation.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * 带 i18n 的 assetsString，文件名应当形如 xxx_zh.json
 * 如果找不到带语言的文件，则使用默认的文件
 * @param name String
 * @return String
 */
@ReadOnlyComposable
@Composable
actual fun assetsStringLocalized(name: String): String {
    TODO("Not yet implemented")
}