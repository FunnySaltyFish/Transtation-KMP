package com.funny.translation.kmp


import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri
import com.eygraber.uri.toUri
import android.net.Uri as AndroidUri

actual class FileLauncher<Input>(
    private val activityResultLauncher: ManagedActivityResultLauncher<Input, AndroidUri?>
) : Launcher<Input, Uri?>() {
    actual override fun launch(input: Input) {
        activityResultLauncher.launch(input)
    }
}

@Composable
actual fun rememberCreateFileLauncher(
    mimeType: String,
    onResult: (Uri?) -> Unit
): FileLauncher<String> {
    val res: ManagedActivityResultLauncher<String, AndroidUri?> = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(mimeType)) {
        onResult(it?.toUri())
    }
    return FileLauncher(res)
}

@Composable
actual fun rememberOpenFileLauncher(
    onResult: (Uri?) -> Unit
): FileLauncher<Array<String>> {
    val res = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        onResult(it?.toUri())
    }
    return FileLauncher(res)
}