package com.funny.translation.translate.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeableState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.ripple
import androidx.compose.material.swipeable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.AppConfig
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.LocalNavController
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.lerp
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.appCtx
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.LLMTranslationResult
import com.funny.translation.translate.Language
import com.funny.translation.translate.TranslationResult
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.main.components.TextTransResultItem
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
import com.funny.translation.ui.NavPaddingItem
import com.funny.translation.ui.safeMain
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.BackHandler

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
    boxSize: Dp = 40.dp,
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
                        },
                        interactionSource = null,
                        indication = ripple(false, boxSize / 2),
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
    tint: Color,
    boxSize: Dp = 48.dp,
    iconSize: Dp = boxSize / 2,
) {
    IconButton(
        onClick = {
            ClipBoardUtil.copy(text.trim())
            appCtx.toastOnUi(ResStrings.snack_finish_copy)
        },
        modifier = modifier.size(boxSize)
    ) {
        FixedSizeIcon(
            Icons.Default.CopyAll,
            contentDescription = ResStrings.copy_content,
            tint = tint,
            modifier = Modifier.size(iconSize)
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
            TextTransResultItem(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(start = 16.dp, end = 8.dp, bottom = 8.dp, top = 0.dp),
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