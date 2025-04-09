package com.funny.translation.translate.ui.floatwindow

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.funny.translation.helper.ProvidePreviewCompositionLocals
import com.funny.translation.translate.ui.main.MainViewModel

@Composable
@Preview
private fun FloatTransWindowPreview() {
    ProvidePreviewCompositionLocals {
        FloatingTranslationWindow(
            MainViewModel().apply {
                translateText = "你好"

            },
            onClose = { },
            onOpenApp = { }
        )
    }
}