package com.funny.translation.translate.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier

private const val TAG = "InputWidget"

@ExperimentalComposeUiApi
@Composable
expect fun InputText(
    modifier: Modifier,
    textProvider: () -> String,
    updateText: (String) -> Unit,
    shouldRequest: Boolean,
    updateFocusRequest: (Boolean) -> Unit,
    translateAction: (() -> Unit)? = null
)