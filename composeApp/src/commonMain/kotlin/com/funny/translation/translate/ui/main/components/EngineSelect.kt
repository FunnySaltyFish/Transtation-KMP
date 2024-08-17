package com.funny.translation.translate.ui.main.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.task.ModelTranslationTask
import com.funny.translation.translate.ui.widget.HintText
import com.funny.translation.ui.AnyPopDialog
import com.funny.translation.ui.popDialogShape

// 用于选择引擎时的回调
internal interface UpdateSelectedEngine {
    fun add(engine: TranslationEngine)
    fun remove(engine: TranslationEngine)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun EngineSelectDialog(
    showDialog: MutableState<Boolean>,
    bindEngines: List<TranslationEngine>,
    jsEngines: List<TranslationEngine>,
    modelEngines: List<TranslationEngine>,
    selectStateProvider: @Composable (TranslationEngine) -> MutableState<Boolean>,
    updateSelectedEngine: UpdateSelectedEngine
) {
    var showEngineSelect by showDialog
    if (showEngineSelect) {
        AnyPopDialog(
            modifier = Modifier.popDialogShape(),
            onDismissRequest = { showEngineSelect = false },
            isActiveClose = false
        ) {
            EngineSelect(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                bindEngines,
                jsEngines,
                modelEngines,
                selectStateProvider,
                updateSelectedEngine
            )
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun EngineSelect(
    modifier: Modifier = Modifier,
    bindEngines: List<TranslationEngine> = arrayListOf(),
    jsEngines: List<TranslationEngine> = arrayListOf(),
    modelEngines: List<TranslationEngine> = arrayListOf(),
    selectStateProvider: @Composable (TranslationEngine) -> MutableState<Boolean>,
    updateSelectEngine: UpdateSelectedEngine
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        EnginePart(
            title = ResStrings.bind_engine,
            engines = bindEngines,
            selectStateProvider = selectStateProvider,
            updateSelectEngine = updateSelectEngine
        )

        if (jsEngines.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            EnginePart(
                title = ResStrings.plugin_engine,
                engines = jsEngines,
                selectStateProvider = selectStateProvider,
                updateSelectEngine = updateSelectEngine
            )
        }

        if (modelEngines.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            EnginePart(
                title = ResStrings.model_engine,
                engines = modelEngines,
                selectStateProvider = selectStateProvider,
                updateSelectEngine = updateSelectEngine
            )
            HintText(text = ResStrings.llm_engine_tip, fontSize = 8.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EnginePart(
    title: String,
    engines: List<TranslationEngine>,
    selectStateProvider: @Composable (TranslationEngine) -> MutableState<Boolean>,
    updateSelectEngine: UpdateSelectedEngine
) {
    Text(
        text = title,
        fontWeight = W600
    )
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp, horizontal = 4.dp),
        horizontalArrangement = spacedBy(8.dp),
        verticalArrangement = spacedBy(0.dp)
    ) {
        engines.forEach { engine ->
            key(engine.name) {
                var taskSelected by selectStateProvider(engine)
                FilterChip(
                    selected = taskSelected,
                    onClick = {
                        if (!taskSelected) { // 选中了
                            updateSelectEngine.add(engine)
                        } else updateSelectEngine.remove(engine)
                        taskSelected = !taskSelected
                    },
                    label = {
                        Text(text = engine.name)
                    },
                    leadingIcon = {
                        if (engine is ModelTranslationTask) {
                            if (engine.model.isFree) {
                                Badge {
                                    Text(text = ResStrings.limited_time_free)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
