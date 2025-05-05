package com.funny.translation.translate.ui.engineselect

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.funny.translation.js.core.JsTranslateTaskText
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.task.ModelTask
import com.funny.translation.translate.ui.long_text.description
import com.funny.translation.translate.ui.plugin.markdown
import com.funny.translation.ui.MarkdownText
import com.funny.translation.ui.RichTooltipCloseButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectEngineChip(
    engine: TranslationEngine,
    selectStateProvider: (TranslationEngine) -> Boolean,
    updateSelectEngine: UpdateSelectedEngine
) {
    var taskSelected = selectStateProvider(engine)
    val tooltipState = rememberTooltipState(isPersistent = true)

    TooltipBox(
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        state = tooltipState,
        tooltip = {
            RichTooltip(
                text = {
                    when (engine) {
                        is ModelTask -> {
                            val model = engine.model
                            Column {
                                Text(
                                    text = model.description(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                val tagDesc = model.tagDescription
                                if (tagDesc != "") {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                    Text(
                                        text = model.tagDescription,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        is JsTranslateTaskText -> {
                            MarkdownText(
                                markdown = engine.jsEngine.jsBean.markdown,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        else -> {
                            Text(
                                text = engine.supportLanguages.joinToString { it.displayText },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                action = {
                    RichTooltipCloseButton(tooltipState)
                }
            )
        }
    ) {
        // 自定义芯片样式
        FilterChip(
            selected = taskSelected,
            onClick = {
                if (!taskSelected) { // 选中了
                    updateSelectEngine.add(engine)
                } else updateSelectEngine.remove(engine)
                taskSelected = !taskSelected
            },
            label = {
                // 引擎名称
                Text(
                    text = engine.name,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                // 标签
                if (engine is ModelTask && engine.model.tag.isNotEmpty()) {
                    val tagColor = when(engine.model.tag) {
                        "Pro限免" -> Color(0xFFE65100)
                        "限免" -> Color(0xFFD32F2F)
                        "强" -> Color(0xFF7B1FA2)
                        "思考" -> Color(0xFF1976D2)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = tagColor.copy(alpha = 0.17f)
                    ) {
                        Text(
                            text = engine.model.tag,
                            color = tagColor,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        )
    }
}

//@Preview
//@Composable
//private fun PreviewEngineChip() {
//    SelectEngineChip(
//        engine = ModelTranslationTask(Model.Empty),
//        selectStateProvider = { mutableStateOf(true) },
//        updateSelectEngine = object : UpdateSelectedEngine {
//            override fun add(engine: TranslationEngine) {
//            }
//
//            override fun remove(engine: TranslationEngine) {
//            }
//        }
//    )
//
//}