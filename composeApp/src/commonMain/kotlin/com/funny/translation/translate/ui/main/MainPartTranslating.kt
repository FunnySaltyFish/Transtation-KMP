package com.funny.translation.translate.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeableState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.AppConfig
import com.funny.translation.GlobalTranslationConfig
import com.funny.translation.bean.show
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.LocalNavController
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.lerp
import com.funny.translation.helper.rememberDerivedStateOf
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.painterDrawableRes
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.LLMTranslationResult
import com.funny.translation.translate.Language
import com.funny.translation.translate.ThinkingStage
import com.funny.translation.translate.TranslationResult
import com.funny.translation.translate.TranslationStage
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.database.transFavoriteDao
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.main.components.ThinkResult
import com.funny.translation.translate.ui.widget.ExpandMoreButton
import com.funny.translation.translate.ui.widget.FrameAnimationIcon
import com.funny.translation.translate.ui.widget.NoticeBar
import com.funny.translation.translate.ui.widget.TwoProgressIndicator
import com.funny.translation.translate.ui.widget.noticeBarModifier
import com.funny.translation.translate.ui.widget.rememberFrameAnimIconState
import com.funny.translation.translate.utils.AudioPlayer
import com.funny.translation.translate.utils.PlaybackState
import com.funny.translation.translate.utils.TTSConfManager
import com.funny.translation.ui.CommonNavBackIcon
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.MarkdownText
import com.funny.translation.ui.NavPaddingItem
import com.funny.translation.ui.TwoTextWithTooltip
import com.funny.translation.ui.safeMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.navigation.BackHandler
import kotlin.math.roundToInt

@Composable
fun MainPartTranslating(vm: MainViewModel) {
    val quitAlertDialog = remember { mutableStateOf(false) }
    SimpleDialog(
        openDialogState = quitAlertDialog,
        title = ResStrings.tip,
        message = ResStrings.quit_translating_alert,
        confirmButtonAction = {
            vm.cancel()
            vm.updateMainScreenState(MainScreenState.Inputting)
        }
    )

    val goBack = remember {
        {
            if (vm.isTranslating()) {
                quitAlertDialog.value = true
            } else {
                vm.cancel()
                vm.updateMainScreenState(MainScreenState.Inputting)
            }
        }
    }

    BackHandler(onBack = goBack)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .run {
                when (LocalWindowSizeState.current) {
                    WindowSizeState.VERTICAL -> windowInsetsPadding(WindowInsets.safeMain.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
                    WindowSizeState.HORIZONTAL -> windowInsetsPadding(WindowInsets.safeMain.only(WindowInsetsSides.End + WindowInsetsSides.Top))
                }
            }
    ) {
        CommonNavBackIcon(navigateBackAction = goBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .run {
                    when (LocalWindowSizeState.current) {
                        WindowSizeState.VERTICAL -> windowInsetsPadding(WindowInsets.safeMain.only(WindowInsetsSides.Horizontal))
                        WindowSizeState.HORIZONTAL -> windowInsetsPadding(WindowInsets.safeMain.only(WindowInsetsSides.End))
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TranslateProgress(startedProgress = vm.startedProgress, finishedProgress = vm.finishedProgress)
            SourceTextPart(
                modifier = Modifier.fillMaxWidth(0.88f),
                sourceText = vm.translateText,
                sourceLanguage = vm.sourceLanguage,
                clearAndGoBackAction = {
                    goBack()
                    if (!vm.isTranslating()) {
                        vm.translateText = ""
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            ResultList(
                modifier = Modifier
                    .fillMaxSize(),
                resultList = vm.resultList,
                doFavorite = vm::doFavorite
            )
        }
    }
}

@Composable
private fun TranslateProgress(
    startedProgress: Float,
    finishedProgress: Float
) {
    // 这个 0.99 （而不是 1） 是为了解决浮点数累加导致的问题
    AnimatedVisibility(visible = finishedProgress < 0.99) {
        TwoProgressIndicator(
            startedProgress = startedProgress,
            finishedProgress = finishedProgress,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SourceTextPart(
    modifier: Modifier,
    sourceText: String,
    sourceLanguage: Language,
    clearAndGoBackAction: SimpleAction
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        val state = rememberSwipeableState(2)
        val scope = rememberCoroutineScope()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = ResStrings.source_text + "(${sourceLanguage.displayText})"
            )
            IconButton(onClick = clearAndGoBackAction) {
                // GoBack And Clear
                FixedSizeIcon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            SpeakButton(
                text = sourceText,
                language = sourceLanguage,
                tint = MaterialTheme.colorScheme.onBackground,
                onStartPlay = {
                    scope.launch {
                        state.animateTo(100)
                    }
                }
            )
            CopyButton(text = sourceText, tint = MaterialTheme.colorScheme.onBackground)
        }
        SwipeableText(text = sourceText, modifier = Modifier.fillMaxWidth(), state = state)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeableText(
    text: String,
    modifier: Modifier,
    state: SwipeableState<Int> = rememberSwipeableState(2)
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .swipeable(
                    state = state,
                    anchors = mapOf(0f to 2, 100f to 100),
                    orientation = Orientation.Vertical,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) },
                )
                .animateContentSize(),
            overflow = TextOverflow.Ellipsis,
            maxLines = lerp( 2, 100, (state.offset.value / 100f).coerceIn(0f, 1f))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth(0.25f)
                .background(Color.LightGray)
                .clip(CircleShape)
        )
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SpeakButton(
    modifier: Modifier = Modifier,
    text: String,
    language: Language,
    tint: Color = MaterialTheme.colorScheme.primary,
    boxSize: Dp = 48.dp,
    iconSize: Dp = boxSize / 2,
    onStartPlay: SimpleAction? = null
) {
    val speakerState = rememberFrameAnimIconState(
        listOf("ic_speaker_2", "ic_speaker_1"),
    )
    val navController = LocalNavController.current
    LaunchedEffect(AudioPlayer.currentPlayingText) {
        // 修正：当列表划出屏幕后state与实际播放不匹配的情况
        if (AudioPlayer.currentPlayingText != text && speakerState.isPlaying) {
            speakerState.reset()
        }
    }
    Box (contentAlignment = Alignment.Center, modifier = modifier.size(boxSize)) {
        val playbackState = AudioPlayer.playbackState
        when {
            playbackState == PlaybackState.LOADING && text == AudioPlayer.currentPlayingText -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(iconSize),
                    strokeWidth = 2.dp,
                )
            }

            else -> {
                Box(
                    modifier = Modifier.size(boxSize).clip(CircleShape).combinedClickable(
                        onLongClick = {
                            TTSConfManager.jumpToEdit(navController, language)
                        },
                        onClick = {
                            if (text == AudioPlayer.currentPlayingText) {
                                speakerState.reset()
                                AudioPlayer.pause()
                            } else {
                                AudioPlayer.playOrPause(
                                    text,
                                    TTSConfManager.findByLanguage(language),
                                    onStartPlay = {
                                        speakerState.play()
                                        onStartPlay?.invoke()
                                    },
                                    onComplete = {
                                        speakerState.reset()
                                    },
                                    onError = {
                                        appCtx.toastOnUi(ResStrings.snack_speak_error)
                                    }
                                )
                            }
                        }
                    ),
                ) {
                    FrameAnimationIcon(
                        state = speakerState,
                        contentDescription = ResStrings.speak,
                        tint = tint,
                        modifier = Modifier.size(iconSize).align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
internal fun CopyButton(
    modifier: Modifier = Modifier,
    text: String,
    tint: Color
) {
    IconButton(
        onClick = {
            ClipBoardUtil.copy(text)
            appCtx.toastOnUi(ResStrings.snack_finish_copy)
        },
        modifier = modifier.size(48.dp)
    ) {
        FixedSizeIcon(
            Icons.Default.CopyAll,
            contentDescription = ResStrings.copy_content,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ResultList(
    modifier: Modifier,
    resultList: SnapshotStateList<TranslationResult>,
    doFavorite: (Boolean, TranslationResult) -> Unit,
) {
    val smartTransEnabled by AppConfig.sAITransExplain
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (!smartTransEnabled && resultList.any { it is LLMTranslationResult }) {
            item {
                SmartTransEnableTip(
                    modifier = Modifier.fillParentMaxWidth()
                )
            }
        }
        itemsIndexed(resultList, key = { _, r -> r.engineName }) { _, result ->
            ResultItem(
                modifier = Modifier.fillMaxWidth(),
                result = result,
                doFavorite = doFavorite,
                smartTransEnabled = smartTransEnabled
            )
        }
        item {
            NavPaddingItem()
        }
    }
}

@Composable
private fun ResultItem(
    modifier: Modifier,
    result: TranslationResult,
    doFavorite: (Boolean, TranslationResult) -> Unit,
    smartTransEnabled : Boolean = false
) {
    val offsetAnim = remember { Animatable(100f) }
    LaunchedEffect(Unit) {
        offsetAnim.animateTo(0f)
    }
    Column(
        modifier = modifier
            .offset { IntOffset(offsetAnim.value.roundToInt(), 0) }
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(start = 16.dp, end = 8.dp, bottom = 8.dp, top = 0.dp)
            .animateContentSize()
    ) {
        var expandDetail by rememberSaveable {
            mutableStateOf(!result.detailText.isNullOrEmpty() && AppConfig.sExpandDetailByDefault.value)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = result.engineName,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.W500
            )

            // 如果有详细释义，则显示展开按钮
            if (!result.detailText.isNullOrEmpty()) {
                Box(
                    modifier = Modifier.offset(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ExpandMoreButton(
                        modifier = Modifier,
                        expand = expandDetail,
                        tint = MaterialTheme.colorScheme.primary
                    ) {
                        expandDetail = it
                    }
                    if (result.stage == TranslationStage.PARTIAL_TRANSLATION) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            // 收藏、朗读、复制三个图标
            var favorite by rememberFavoriteState(result = result)
            IconButton(onClick = {
                doFavorite(favorite, result)
                favorite = !favorite
            }, modifier = Modifier.offset(x = 16.dp)) {
                FixedSizeIcon(
                    imageVector = if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = ResStrings.favorite,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            SpeakButton(
                modifier = Modifier.offset(8.dp),
                text = result.basic,
                language = result.targetLanguage!!
            )
            CopyButton(
                text = result.basic,
                tint = MaterialTheme.colorScheme.primary
            )
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
                    )
                }
            }
        }
        if (expandDetail && result.detailText != null) {
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
private fun SmartTransEnableTip(
    modifier: Modifier
) {
    val navController = LocalNavController.current
    NoticeBar(
        modifier = modifier.noticeBarModifier {
            navController.navigate(TranslateScreen.SettingScreen.route)
        },
        text = ResStrings.smart_trans_enable_tip,
        singleLine = true
    )
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
fun CostIndicator(
    modifier: Modifier = Modifier,
    selectingPromptCost: String,
    actualCost: String,
    totalCost: String,
    supportingString: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    TwoTextWithTooltip(
        modifier = modifier,
        text = totalCost,
        text1 = selectingPromptCost,
        text1Desc = ResStrings.selecting_prompt_cost,
        text2 = actualCost,
        text2Desc = ResStrings.actual_cost,
        style = LocalTextStyle.current.copy(
            fontSize = 12.sp,
        ),
        supportingText = supportingString,
        contentColor = color
    )
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