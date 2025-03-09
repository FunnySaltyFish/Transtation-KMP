package com.funny.translation.translate.ui.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ImageTransMain(
    imageUri: String?,
    sourceId: Int?,
    targetId: Int?,
    doClipFirst: Boolean,
    updateCurrentPage: (ImageTransPage) -> Unit
) {
}

@Composable
actual fun ResultPart(
    modifier: Modifier,
    vm: ImageTransViewModel
) {
}