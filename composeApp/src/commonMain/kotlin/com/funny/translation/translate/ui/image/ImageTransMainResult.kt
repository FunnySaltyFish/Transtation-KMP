package com.funny.translation.translate.ui.image

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.funny.cmaterialcolors.MaterialColors
import com.funny.compose.loading.LoadingState
import com.funny.translation.bean.show
import com.funny.translation.helper.Log
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.Cost
import com.funny.translation.translate.ImageTranslationResult
import com.funny.translation.translate.ui.main.CostIndicator
import com.funny.translation.ui.AutoResizedText
import com.funny.translation.ui.MarkdownText
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.zooming
import kotlinx.coroutines.delay
import java.math.BigDecimal
import kotlin.math.max

@Composable
internal fun NormalTransResult(
    data: ImageTranslationResult.Normal,
    showResult: Boolean,
    translateState: LoadingState<ImageTranslationResult>,
    contentSize: IntSize,
    zoomableState: ZoomableState,
    imageInitialScale: Float,
) {
    if (translateState.isLoading) {
        CircularProgressIndicator(
            Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
    } else if (translateState.isSuccess) {
        val alpha by animateFloatAsState(targetValue = if (showResult) 1f else 0f)
        with(LocalDensity.current) {
            var boxHeight by remember(contentSize) { mutableIntStateOf(contentSize.height) }
            var maxY by rememberStateOf(0)

            LaunchedEffect(maxY) {
                delay(50) // 防抖
                // 由于外部下采样的存在，这个 contentSize 可能实际上矮于实际内容，这时候需要修正一下
                if (maxY > boxHeight) {
                    boxHeight = maxY
                    Log.d("NormalTransResult", "update boxHeight: $boxHeight")
                }
            }

            Box(Modifier
                .fillMaxSize()
                .zooming(zoomableState)) {
                Box(
                    Modifier
                        .width(contentSize.width.toDp())
                        .height(boxHeight.toDp())
                        .alpha(alpha)
                        .background(Color.LightGray.copy(0.9f))
                        .border(2.dp, color = Color.White)
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
                                            (part.y * imageInitialScale).toInt().also { y ->
                                                // 由于下采样的存在，更新一下 maxH
                                                maxY = max(maxY, y)
                                            }
                                        )
                                    }
                                    .border(1.dp, color = Color.White),
                                text = part.target,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ModelTransResult(
    data: ImageTranslationResult.Model,
    showResult: Boolean,
    stage: TranslateStage,
) {
    val density = LocalDensity.current
    BoxWithConstraints {
        with(density) {
            val maxWidth = (constraints.maxWidth * 4 / 5).toDp()
            val maxHeight = (constraints.maxHeight * 4 / 5).toDp()
            val initialX = (constraints.maxWidth - maxWidth.toPx()) / 2
            DraggableBox(
                modifier = Modifier
                    .widthIn(max = if (showResult) maxWidth else 0.dp)
                    .heightIn(max = maxHeight)
                    .align(Alignment.TopCenter)
                    .background(Color.LightGray.copy(0.9f), RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
                initialOffset = Offset(x = initialX, y = 16.dp.toPx())
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                ) {
                    MarkdownText(
                        modifier = Modifier.fillMaxWidth(),
                        markdown = data.streamingResult.ifBlank { ResStrings.translating },
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
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DraggableBox(
    modifier: Modifier,
    initialOffset: Offset,
    content: @Composable () -> Unit
) {
    // set up all transformation states
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(initialOffset) }
    // let's create a modifier state to specify how to update our UI state defined above
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }
    val draggableState = rememberDraggable2DState { offset += it }
    Box(
        modifier = Modifier
            // apply pan offset state as a layout transformation before other modifiers
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.rotationZ = rotation
                this.translationX = offset.x
                this.translationY = offset.y
            }
            .then(modifier)
            // add transformable to listen to multitouch transformation events after offset
            // To make sure our transformable work well within pager or scrolling lists,
            // disallow panning if we are not zoomed in.
            .transformable(state = state, canPan = { scale != 1f })
            .draggable2D(draggableState),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun PreviewModelTransResult() {
    Box(Modifier.fillMaxSize().background(MaterialColors.Amber100)) {
        ModelTransResult(
            data = ImageTranslationResult.Model().apply {
                streamingResult = """
                    ### Hello, World!
                    nice to meet **you**
                    
                    | 菜名 | 价格(k) |
                    |---|---|
                    |  螺蛳粉 | 25 |
                    |  豆花 | 30 |
                    |  豆花+香肠 | 35 |
                    |  豆花+炸猪油渣 | 35 |
                    |  豆花+鸡蛋 | 40 |
                    |  豆花+牛肉 | 40 |
                    |  豆花+濑尿虾 | 45 |
                    |  豆花+香肠+炸猪油渣 | 40 |
                    |  豆花+香肠+牛肉 | 45 |
                    |  豆花+香肠+鸡蛋 | 45 |
                """.trimIndent()
                error = ""
                cost = Cost(
                    consumption = BigDecimal.ONE,
                    input_tokens = 23,
                    output_tokens = 1234
                )
            },
            showResult = true,
            stage = TranslateStage.Finished
        )
    }
}

private const val INVALID_TEXT = "0"