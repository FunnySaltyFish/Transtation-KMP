package com.funny.translation.kmp

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri

/**
 * KMP Launcher
 * @author FunnySaltyFish
 */
abstract class Launcher<Input, Output> {
    abstract fun launch(input: Input)
}

expect class FileLauncher<Input>: Launcher<Input, Uri?> {
    override fun launch(input: Input)
}

expect class MultiFileLauncher<Input>: Launcher<Input, List<String>> {
    override fun launch(input: Input)
}


@Composable
expect fun rememberCreateFileLauncher(
    mimeType: String = "*/*",
    onResult: (Uri?) -> Unit = {},
): FileLauncher<String>

@Composable
expect fun rememberOpenFileLauncher(
    onResult: (Uri?) -> Unit = {},
): FileLauncher<Array<String>>

@Composable
expect fun rememberTakePhotoLauncher(
    onResult: (Boolean) -> Unit = {},
): Launcher<String, Boolean>



//@Composable
//expect fun <Input> rememberSelectPhotoUriLauncher(
//    onResult: (Uri?) -> Unit = {},
//): FileLauncher<Input>