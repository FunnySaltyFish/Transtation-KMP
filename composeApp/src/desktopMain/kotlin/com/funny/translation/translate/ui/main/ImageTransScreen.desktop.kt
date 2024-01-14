package com.funny.translation.translate.ui.main

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri
import com.funny.translation.ui.Working

@Composable
actual fun ImageTransScreen(
    imageUri: Uri?,
    sourceId: Int?,
    targetId: Int?,
    doClipFirst: Boolean
) {
    Working()
}