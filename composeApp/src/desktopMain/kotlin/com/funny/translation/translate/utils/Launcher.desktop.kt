package com.funny.translation.translate.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.funny.translation.kmp.MultiFileLauncher

@Composable
actual fun rememberSelectImageLauncher(
    maxNum: Int,
    pickedItems: List<String>,
    onResult: (List<String>) -> Unit
): MultiFileLauncher<Array<String>> {
    return remember(mimeTypes, maxNum, pickedItems) { MultiFileLauncher(onResult) }
}