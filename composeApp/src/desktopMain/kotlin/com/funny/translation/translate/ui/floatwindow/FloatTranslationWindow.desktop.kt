package com.funny.translation.translate.ui.floatwindow

import androidx.compose.runtime.Composable
import com.funny.translation.translate.TranslationEngine

@Composable
actual fun EngineSelectDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSelect: (TranslationEngine) -> Unit
) {

}