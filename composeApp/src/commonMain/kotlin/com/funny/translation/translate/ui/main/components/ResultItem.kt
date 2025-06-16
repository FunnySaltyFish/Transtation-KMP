package com.funny.translation.translate.ui.main.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.AppConfig
import com.funny.translation.GlobalTranslationConfig
import com.funny.translation.bean.show
import com.funny.translation.helper.Log
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.rememberDerivedStateOf
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.kmp.painterDrawableRes
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.LLMTranslationResult
import com.funny.translation.translate.ThinkingStage
import com.funny.translation.translate.TranslationResult
import com.funny.translation.translate.TranslationStage
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.database.transFavoriteDao
import com.funny.translation.translate.ui.main.CopyButton
import com.funny.translation.translate.ui.main.SpeakButton
import com.funny.translation.translate.ui.widget.ExpandButton
import com.funny.translation.translate.ui.widget.ExpandState
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.MarkdownText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
internal fun TextTransResultItem(
    modifier: Modifier,
    result: TranslationResult,
    doFavorite: (Boolean, TranslationResult) -> Unit,
    stopTranslateAction: (result: TranslationResult) -> Unit,
    smartTransEnabled : Boolean = false
) {
    Column(
        modifier = modifier
    ) {
        val expandBasicResultThreshold = 4
        val hasDetailText by rememberDerivedStateOf {
            !result.detailText.isNullOrEmpty()
        }
        val alwaysExpand = AppConfig.sExpandDetailByDefault.value
        var textLayoutLine by rememberStateOf(0)
        val canExpandBasic by rememberDerivedStateOf {
            result.stage.isEnd() && textLayoutLine > expandBasicResultThreshold
        }
        var expandState by rememberSaveable(hasDetailText) {
            val value = when {
                hasDetailText  -> {
                    if (canExpandBasic && alwaysExpand) ExpandState.FULL // 既可以二次展开，又默认展开
                    else if (canExpandBasic) ExpandState.PARTIAL         // 仅能二次展开，默认收起
                    else if (alwaysExpand) ExpandState.FULL              // 不能二次展开，但默认展开
                    else ExpandState.COLLAPSED                           // 不能二次展开，也不默认展开，收起
                }
                else -> ExpandState.FULL
            }
            mutableStateOf(value)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = result.engineName,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.W500
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((-8).dp),
            ) {
                // 如果有详细释义，则显示展开按钮
                if (hasDetailText || canExpandBasic) {
                    ExpandResultButton(
                        result = result,
                        supportTwoLevel = hasDetailText && canExpandBasic,
                        expandState = expandState,
                        onExpandStateChange = {
                            Log.d("TextTransResultItem", "Expand state changed from $expandState to $it")
                            expandState = it
                        }
                    )
                }
                if (result.stage.isEnd()) {
                    FunctionRowFinished(result, doFavorite, onStartPlay = {
                        if (expandState == ExpandState.COLLAPSED && canExpandBasic) {
                            expandState = ExpandState.PARTIAL
                        }
                    })
                } else {
                    FunctionRowTranslating(result, stopTranslateAction)
                }
            }
        }

        SelectionContainer {
            Column {
                if (result.thinkStage != ThinkingStage.IDLE) {
                    ThinkResult(modifier = Modifier.padding(end = 8.dp), result = result)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (result.thinkStage != ThinkingStage.THINKING) {
                    Text(
                        text = buildAnnotatedString {
                            append(result.basic.trim().ifEmpty { "正在翻译中……" })
                            if (result.error.isNotEmpty()) {
                                append(" ")
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                                    append(result.error)
                                }
                            }
                        },
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 16.sp,
                        maxLines = if (canExpandBasic && expandState == ExpandState.COLLAPSED) expandBasicResultThreshold else Int.MAX_VALUE,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = {
                            // 如果 canExpandBasic 已经算好了，就别更新了
                            if (!canExpandBasic) textLayoutLine = it.lineCount
                        }
                    )
                }
            }
        }
        if (expandState == ExpandState.FULL && hasDetailText) {
            HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
            MarkdownText(
                markdown = result.detailText!!,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                selectable = true
            )
        }

        if (result is LLMTranslationResult) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (smartTransEnabled) {
                    CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )) {
                        SmartTransIndicator(
                            modifier = Modifier,
                            result = result
                        )
                    }
                }

                val cost = result.cost
                CostIndicator(
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End),
                    selectingPromptCost = cost.selectingPrompt.consumption.show(6),
                    actualCost = cost.actualTrans.consumption.show(6),
                    totalCost = cost.total.show(6),
                    supportingString = ResStrings.llm_trans_template.format(
                        input1 = cost.selectingPrompt.input_tokens.toString(),
                        output1 = cost.selectingPrompt.output_tokens.toString(),
                        input2 = cost.actualTrans.input_tokens.toString(),
                        output2 = cost.actualTrans.output_tokens.toString()
                    ),
                )
            }
        }
    }
}

@Composable
private fun ExpandResultButton(
    result: TranslationResult,
    supportTwoLevel: Boolean,
    expandState: ExpandState,
    onExpandStateChange: (ExpandState) -> Unit
) {
    Box(
        modifier = Modifier,
        contentAlignment = Alignment.Center
    ) {
        ExpandButton(
            modifier = Modifier,
            expandState = expandState,
            supportTwoLevel = supportTwoLevel,
            tint = MaterialTheme.colorScheme.primary,
            onExpandChange = onExpandStateChange
        )
        if (result.stage == TranslationStage.PARTIAL_TRANSLATION) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun FunctionRowFinished(
    result: TranslationResult,
    doFavorite: (Boolean, TranslationResult) -> Unit,
    onStartPlay: SimpleAction
) {
    // 收藏、朗读、复制三个图标
    var favorite by rememberFavoriteState(result = result)
    IconButton(onClick = {
        doFavorite(favorite, result)
        favorite = !favorite
    }, modifier = Modifier) {
        FixedSizeIcon(
            imageVector = if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = ResStrings.favorite,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
    SpeakButton(
        modifier = Modifier,
        text = result.basic,
        language = result.targetLanguage!!,
        onStartPlay = onStartPlay
    )
    CopyButton(
        text = result.basic,
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun FunctionRowTranslating(
    result: TranslationResult,
    stopTranslateAction: (result: TranslationResult) -> Unit,
) {
    // 暂停
    IconButton(
        onClick = { stopTranslateAction(result) },
        modifier = Modifier
    ) {
        FixedSizeIcon(
            Icons.Rounded.Pause,
            contentDescription = "Stop Translating This",
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

sealed class SmartTransIndicatorShowType {
    data object None : SmartTransIndicatorShowType()
    data object Selecting : SmartTransIndicatorShowType()
    data class Result(val type: String) : SmartTransIndicatorShowType() {
        override fun toString(): String {
            return "Result#$type"
        }
    }

    companion object {
        val Saver = Saver<SmartTransIndicatorShowType, String>(
            save = { it.toString() },
            restore = {
                when (it) {
                    "None" -> None
                    "Selecting" -> Selecting
                    else -> Result(it.substringAfter("#"))
                }
            }
        )
    }
}

@Composable
private fun SmartTransIndicator(
    modifier: Modifier = Modifier,
    result: TranslationResult,
) {
    val stage by rememberUpdatedState(result.stage)
    val showType by rememberDerivedStateOf {
        when {
            stage == TranslationStage.IDLE -> SmartTransIndicatorShowType.None
            stage == TranslationStage.SELECTING_PROMPT -> SmartTransIndicatorShowType.Selecting
            stage >= TranslationStage.SELECTED_PROMPT && result.smartTransType != null ->
                SmartTransIndicatorShowType.Result(result.smartTransType!!)

            else -> SmartTransIndicatorShowType.None
        }
    }

    AnimatedContent(modifier = modifier, targetState = showType) { type ->
        when (type) {
            SmartTransIndicatorShowType.None -> {}
            SmartTransIndicatorShowType.Selecting -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("...")
                }
            }

            is SmartTransIndicatorShowType.Result -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FixedSizeIcon(
                        painter = painterDrawableRes("ic_magic"),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(type.type)
                }
            }
        }
    }
}



@Composable
private fun rememberFavoriteState(
    result: TranslationResult
): MutableState<Boolean> {
    val state = remember { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        if (!GlobalTranslationConfig.isValid()) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val hasFavorited = appDB.transFavoriteDao.count(
                GlobalTranslationConfig.sourceString!!,
                result.basic,
                GlobalTranslationConfig.sourceLanguage!!.id,
                GlobalTranslationConfig.targetLanguage!!.id,
                result.engineName
            ) > 0
            withContext(Dispatchers.Main) {
                state.value = hasFavorited
            }
        }
    }
    return state
}