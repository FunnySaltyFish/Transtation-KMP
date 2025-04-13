package com.funny.translation.translate.ui.floatwindow

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.SystemAlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.SystemDialogProperties
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.ui.main.components.EngineSelect
import com.funny.translation.translate.ui.main.components.UpdateSelectedEngine
import com.funny.translation.translate.ui.settings.FloatWindowEngineSelect

@OptIn(ExperimentalAnimationApi::class)
@Composable
actual fun EngineSelectDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSelect: (TranslationEngine) -> Unit
) {
    if (show) {
        FloatWindowEngineSelect(
            modifier = Modifier,
            wrapper = { modifier, bindEngines, jsEngines, modelEngines, selectStateProvider, updateSelectedEngine ->
                SystemAlertDialog(
                    onDismissRequest = onDismiss,
                    text = {
                        EngineSelect(
                            bindEngines = bindEngines,
                            jsEngines = jsEngines,
                            modelEngines = modelEngines,
                            selectStateProvider = selectStateProvider,
                            updateSelectEngine = object : UpdateSelectedEngine by updateSelectedEngine {
                                override fun add(engine: TranslationEngine) {
                                    updateSelectedEngine.add(engine)
                                    onSelect(engine)
                                }
                            },
                            dismissDialogAction = onDismiss
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onDismiss()
                            }
                        ) {
                            Text(text = ResStrings.close)
                        }
                    },
                    properties = SystemDialogProperties(
                        usePlatformDefaultWidth = false,
                    )
                )
            }
        )
    }
}