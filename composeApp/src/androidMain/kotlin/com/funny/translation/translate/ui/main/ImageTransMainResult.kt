package com.funny.translation.translate.ui.main

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cn.qhplus.emo.photo.ui.GestureContentState
import com.funny.translation.bean.show
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ImageTranslationResult
import com.funny.translation.ui.AutoResizedText
import com.funny.translation.ui.MarkdownText

@Composable
internal fun BoxScope.NormalTransResult(
    data: ImageTranslationResult.Normal,
    gestureState: GestureContentState,
    lazyListState: LazyListState,
    imageInitialScale: Float,
) {
    val layoutInfo = gestureState.layoutInfo ?: return
    Box(
        Modifier
            .width(layoutInfo.contentWidth)
            .height(layoutInfo.contentHeight)
            .align(Alignment.Center)
            .border(2.dp, color = Color.White)
            .offset {
                IntOffset(
                    0,
                    (-(lazyListState.firstVisibleItemScrollOffset + lazyListState.firstVisibleItemIndex * layoutInfo.px.containerHeight)).toInt()
                )
            }
    ) {
        SelectionContainer {
            val density = LocalDensity.current
            data.content.forEach { part ->
                val w =
                    remember { (part.width * imageInitialScale / density.density).dp }
                val h =
                    remember { (part.height * imageInitialScale / density.density).dp }
                AutoResizedText(
                    modifier = Modifier
                        .requiredSize(w, h)
                        .offset {
                            IntOffset(
                                (part.x * imageInitialScale).toInt(),
                                (part.y * imageInitialScale).toInt()
                            )
                        },
                    text = part.target,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
internal fun BoxScope.ModelTransResult(
    data: ImageTranslationResult.Model,
    stage: TranslateStage
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState()),
    ) {
        MarkdownText(
            modifier = Modifier,
            markdown = data.streamingResult,
            color = Color.White,
            selectable = true
        )
        if (data.error.isNotEmpty()) {
            Text(text = data.error, color = MaterialTheme.colorScheme.error)
        }
        if (stage == TranslateStage.Finished) {
            HorizontalDivider(color = Color.White)
            val cost = data.cost
            CostIndicator(
                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End),
                selectingPromptCost = INVALID_TEXT,
                actualCost = cost.consumption.show(6),
                totalCost = cost.consumption.show(6),
                supportingString = ResStrings.llm_trans_template.format(
                    input1 = INVALID_TEXT,
                    output1 = INVALID_TEXT,
                    input2 = cost.input_tokens.toString(),
                    output2 = cost.output_tokens.toString()
                ),
                color = Color.White
            )
        }
    }
}

private const val INVALID_TEXT = "0"