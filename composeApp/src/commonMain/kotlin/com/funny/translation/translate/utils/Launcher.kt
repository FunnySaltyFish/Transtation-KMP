package com.funny.translation.translate.utils

import androidx.compose.runtime.Composable
import com.funny.translation.kmp.MultiFileLauncher

data class LocalPhoto(
    val uri: String,
    val width: Int = -1,
    val height: Int = -1,
    val rotation: Int = 0,
)

@Composable
expect fun rememberSelectImageLauncher(
    maxNum: Int = 1,
    pickedItems: List<String>,
    onResult: (List<String>) -> Unit = {},
): MultiFileLauncher<Array<String>>