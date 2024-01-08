package com.funny.translation.translate.ui.main

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi

private const val TAG = "ImageTransScreen"

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
expect fun ImageTransScreen(
    imageUri: Uri? = null,
    sourceId: Int? = null,
    targetId: Int? = null,
    doClipFirst: Boolean = false
)