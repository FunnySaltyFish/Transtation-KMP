package com.funny.translation.translate.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.funny.translation.kmp.Launcher
import com.funny.translation.kmp.MultiFileLauncher
import java.awt.Desktop
import java.io.File

@Composable
actual fun rememberSelectImageLauncher(
    maxNum: Int,
    pickedItems: List<String>,
    onResult: (List<String>) -> Unit
): MultiFileLauncher<Array<String>> {
    return remember(maxNum, pickedItems) { MultiFileLauncher(onResult) }
}

actual class InstallApkLauncher(
    private val onResult: (Boolean) -> Unit
): Launcher<File, Boolean>() {
    override fun launch(input: File) {
        try {
            Desktop.getDesktop().open(input.parentFile)
            onResult(true)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false)
        }
    }
}

@Composable
actual fun rememberInstallApkLauncher(onResult: (Boolean) -> Unit): InstallApkLauncher {
    return remember(onResult) {
        InstallApkLauncher(onResult)
    }
}