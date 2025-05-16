package com.funny.translation.translate.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.toast
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.LocalKMPContext
import com.funny.translation.kmp.painterDrawableRes
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ui.widget.InputText
import com.funny.translation.translate.ui.widget.UpperPartBackground
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.SubcomposeBottomFirstLayout
import kotlinx.coroutines.delay
import moe.tlaster.precompose.navigation.BackHandler
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Composable
fun MainPartInputting(
    vm: MainViewModel,
    showSnackbar: (String) -> Unit,
    showEngineSelectAction: SimpleAction
) {
    val context = LocalKMPContext.current
    fun startTranslate() {
        val selectedEngines = vm.selectedEngines
        if (selectedEngines.isEmpty()) {
            context.toastOnUi(ResStrings.snack_no_engine_selected)
            return
        }
        val selectedSize = selectedEngines.size
        if (selectedSize > Consts.MAX_SELECT_ENGINES) {
            val tip = if (AppConfig.isMembership()) {
                ResStrings.message_out_of_max_engine_limit.format(
                    Consts.MAX_SELECT_ENGINES.toString(),
                    selectedSize.toString())
            } else
                ResStrings.message_out_of_max_engine_limit_novip.format(
                    Consts.MAX_SELECT_ENGINES.toString(),
                )
            showSnackbar(tip)
            return
        }
        if (!vm.translating) {
            vm.translate()
//            shouldRequestFocus = false
        } else {
            vm.cancel()
            context.toastOnUi(ResStrings.message_stop_translate)
        }
    }
    val goBackAction = remember {
        {
            vm.updateMainScreenState(MainScreenState.Normal)
            vm.translateText = ""
        }
    }
    BackHandler(onBack = goBackAction)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UpperPartBackground(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .run {
                        when (LocalWindowSizeState.current) {
                            WindowSizeState.VERTICAL -> windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                            WindowSizeState.HORIZONTAL -> windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.End))
                    }
                }
            ) {
                MainTopBarInputting(
                    showEngineSelectAction = showEngineSelectAction,
                    navigateBackAction = goBackAction
                )
                InputPart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    vm = vm,
                    startTranslateActon = ::startTranslate
                )
            }
        }
        LanguageSelectRow(
            modifier = Modifier
                .fillMaxWidth()
                .run {
                    when (LocalWindowSizeState.current) {
                        WindowSizeState.VERTICAL -> windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                        WindowSizeState.HORIZONTAL -> windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.End))
                    }
                }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            sourceLanguage = vm.sourceLanguage,
            updateSourceLanguage = vm::updateSourceLanguage,
            targetLanguage = vm.targetLanguage,
            updateTargetLanguage = vm::updateTargetLanguage,
        )
    }
}

@Composable
private fun TranslateAndClearRow(
    vm: MainViewModel,
    startTranslateActon: SimpleAction
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 6.dp, top = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        ElevatedButton(
            onClick = { vm.translateText = "" },
            modifier = Modifier
                .padding(end = 8.dp)
        ) {
            Text(
                text = ResStrings.clear_content,
                color = MaterialTheme.colorScheme.error
            )
        }
        ElevatedButton(
            onClick = startTranslateActon,
            modifier = Modifier
        ) {
            Text(
                text = ResStrings.translate,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
private fun MainTopBarInputting(
    navigateBackAction: SimpleAction,
    showEngineSelectAction: SimpleAction
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = navigateBackAction) {
                FixedSizeIcon(
                    Icons.Default.ArrowBack,
                    contentDescription = ResStrings.back
                )
            }
        },
        actions = {
            // 开关智能翻译
            var smartTransEnabled by AppConfig.sAITransExplain
            IconToggleButton(
                checked = smartTransEnabled,
                onCheckedChange = {
                    smartTransEnabled = it
                    if (it) toast(ResStrings.ai_trans_explain_enabled)
                }
            ) {
                val icon = Icons.Default.Insights
                val tint = if (smartTransEnabled) MaterialTheme.colorScheme.primary else MaterialColors.Grey400
                FixedSizeIcon(icon, contentDescription = ResStrings.ai_trans_explain, tint = tint)
            }
            IconButton(onClick = showEngineSelectAction) {
                FixedSizeIcon(
                    painterDrawableRes("ic_translate"),
                    contentDescription = ResStrings.engine_select,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun InputPart(
    modifier: Modifier,
    vm: MainViewModel,
    startTranslateActon: SimpleAction
) {
    var shouldRequestFocus by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // 等待绘制完成后再请求焦点
        delay(100)
        shouldRequestFocus = true
    }

    SubcomposeBottomFirstLayout(
        modifier,
        bottom = {
            val rowVisible by remember { derivedStateOf { vm.actualTransText.isNotEmpty() }}
            AnimatedVisibility(
                visible = rowVisible,
                enter = slideInVertically { fullHeight -> fullHeight } + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }) + fadeOut()
            ) {
                TranslateAndClearRow(vm, startTranslateActon = startTranslateActon)
            }
        }
    ) {
        InputText(
            modifier = Modifier
                .fillMaxSize(),
            textProvider = { vm.translateText },
            updateText = {
                vm.updateTranslateText(it)
            },
            shouldRequest = shouldRequestFocus,
            updateFocusRequest = {
                if (it != shouldRequestFocus) shouldRequestFocus = it
            },
            translateAction = startTranslateActon
        )
    }
}
