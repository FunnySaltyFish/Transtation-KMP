package com.funny.translation.translate.utils

import androidx.compose.runtime.Composable
import com.funny.translation.kmp.Launcher
import com.funny.translation.kmp.MultiFileLauncher
import java.io.File

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

expect class InstallApkLauncher: Launcher<File, Boolean>

@Composable
expect fun rememberInstallApkLauncher(
    onResult: (Boolean) -> Unit = {},
): InstallApkLauncher