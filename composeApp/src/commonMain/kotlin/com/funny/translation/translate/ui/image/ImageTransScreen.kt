package com.funny.translation.translate.ui.image

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.compose.loading.LoadingState
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.helper.CacheManager
import com.funny.translation.helper.Log
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.kmp.BackHandler
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ImageTranslationResult
import com.funny.translation.translate.Language
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.enabledLanguages
import com.funny.translation.translate.engine.ImageTranslationEngine
import com.funny.translation.translate.ui.main.components.EngineSelectDialog
import com.funny.translation.translate.ui.main.components.UpdateSelectedEngine
import com.funny.translation.translate.ui.widget.ExchangeButton
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.floatingActionBarModifier

internal const val TAG = "ImageTransScreen"

@Composable
fun ImageTransScreen(
    imageUri: String? = null,
    sourceId: Int? = null,
    targetId: Int? = null,
    doClipFirst: Boolean = false
) {
    var currentPage by rememberStateOf(ImageTransPage.Main)
    val updateCurrentPage = remember {
        { page: ImageTransPage -> currentPage = page }
    }
    when (currentPage) {
        ImageTransPage.Main -> ImageTransMain(imageUri, sourceId, targetId, doClipFirst, updateCurrentPage)
        ImageTransPage.ResultList -> ImageTransResultList(updateCurrentPage)
    }
}

@Composable
expect fun ImageTransMain(
    imageUri: String?,
    sourceId: Int?,
    targetId: Int?,
    doClipFirst: Boolean,
    updateCurrentPage: (ImageTransPage) -> Unit
)

@Composable
internal fun ImageTranslationPart(
    vm: ImageTransViewModel,
    updateCurrentPage: (ImageTransPage) -> Unit
) {
    val goBackTipDialogState = remember {
        mutableStateOf(false)
    }
    val currentEnabledLanguages by enabledLanguages.collectAsState()
    val goBack = remember {
        {
            if (vm.isTranslating()) goBackTipDialogState.value = true
            else vm.updateImageUri(null)
        }
    }

    SimpleDialog(
        openDialogState = goBackTipDialogState,
        ResStrings.tip,
        "当前翻译正在进行中，您确定要退出吗？",
        confirmButtonAction = {
            vm.updateImageUri(null)
        }
    )

    BackHandler(enabled = true, onBack = goBack)

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = goBack) {
                FixedSizeIcon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            LanguageSelectRow(
                modifier = Modifier,
                sourceLanguage = vm.sourceLanguage,
                updateSourceLanguage = vm::updateSourceLanguage,
                targetLanguage = vm.targetLanguage,
                updateTargetLanguage = vm::updateTargetLanguage,
                enabledLanguages = currentEnabledLanguages,
                textColor = MaterialTheme.colorScheme.onBackground
            )
            EngineSelect(
                engine = vm.translateEngine,
                updateEngine = vm::updateTranslateEngine,
                bindEngines = vm.bindEngines,
                modelEngines = vm.modelEngines
            )
        }
        ResultPart(modifier = Modifier.fillMaxSize(), vm = vm)
    }

    FloatButtonRow(
        translateStage = vm.translateStage,
        translateState = vm.translateState,
        translateAction = vm::translate,
        stopAction = vm::stopTranslate,
        showResultState = vm.showResultState,
        gotoResultListAction = { updateCurrentPage(ImageTransPage.ResultList) }
    )
}

@Composable
expect fun ResultPart(
    modifier: Modifier,
    vm: ImageTransViewModel
)

@Composable
private fun FloatButtonRow(
    translateStage: TranslateStage,
    translateState: LoadingState<ImageTranslationResult>,
    translateAction: SimpleAction,
    stopAction: SimpleAction,
    showResultState: MutableState<Boolean>,
    gotoResultListAction: SimpleAction
) {
    Row(
        modifier = Modifier.floatingActionBarModifier(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (translateStage) {
            TranslateStage.IDLE, TranslateStage.Finished -> {
                FloatingActionButton(
                    onClick = { translateAction() },
                    modifier = Modifier
                ) {
                    // 开始翻译
                    FixedSizeIcon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Translate",
                    )
                }
            }

            TranslateStage.Translating, TranslateStage.Outputting -> {
                // 翻译中
                FloatingActionButton(
                    onClick = stopAction,
                    modifier = Modifier
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        if (translateStage == TranslateStage.Outputting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = LocalContentColor.current,
                                strokeWidth = (1.5).dp
                            )
                        }
                        // 停止翻译
                        FixedSizeIcon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                        )
                    }
                }
            }
        }

        if (translateState.isSuccess) {
            // 解析结果
            FloatingActionButton(
                onClick = { showResultState.value = !showResultState.value },
                modifier = Modifier
            ) {
                // 解析结果
                FixedSizeIcon(
                    imageVector = if (showResultState.value) Icons.Default.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = "Toggle Result",
                )
            }

            if (translateState.getAsNormal() != null) {
                // 更改透明度
                FloatingActionButton(
                    onClick = gotoResultListAction,
                    modifier = Modifier
                ) {
                    // 解析结果
                    FixedSizeIcon(
                        imageVector = Icons.Filled.ViewList,
                        contentDescription = "Next",
                    )
                }
            }
        }
    }
}

@Composable
internal fun EngineSelect(
    engine: ImageTranslationEngine,
    updateEngine: (ImageTranslationEngine) -> Unit,
    bindEngines: List<ImageTranslationEngine>,
    modelEngines: List<ImageTranslationEngine>
) {
    val showDialog = rememberStateOf(false)
    var selectEngine by rememberStateOf(engine)

    val states = remember(bindEngines, modelEngines) {
        Log.d("FloatWindowScreen", "remember states triggered")
        hashMapOf<TranslationEngine, MutableState<Boolean>>().apply {
            bindEngines.forEach { put(it, mutableStateOf(it == engine)) }
            modelEngines.forEach { put(it, mutableStateOf(it == engine)) }
        }
    }

    LaunchedEffect(selectEngine) {
        states.forEach {
            it.value.value = (it.key == selectEngine)
        }
        updateEngine(selectEngine)
    }

    EngineSelectDialog(
        showDialog = showDialog,
        bindEngines = bindEngines,
        jsEngines = emptyList(),
        modelEngines = modelEngines,
        selectStateProvider = { states[it] ?: rememberStateOf(false) },
        updateSelectedEngine = object : UpdateSelectedEngine {
            override fun add(engine: TranslationEngine) {
                selectEngine = engine as ImageTranslationEngine
            }

            override fun remove(engine: TranslationEngine) {
                states[engine]?.value = false
            }
        }
    )
    TextButton(onClick = { showDialog.value = true }) {
        Text(text = engine.name)
    }
}

@Composable
internal fun LanguageSelectRow(
    modifier: Modifier,
    exchangeButtonTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    sourceLanguage: Language,
    updateSourceLanguage: (Language) -> Unit,
    targetLanguage: Language,
    updateTargetLanguage: (Language) -> Unit,
    enabledLanguages: List<Language>,
    textColor: Color = Color.White
) {
    Row(modifier.horizontalScroll(rememberScrollState())) {
        LanguageSelect(
            Modifier.semantics {
                contentDescription = ResStrings.des_current_source_lang
            },
            language = sourceLanguage,
            languages = enabledLanguages,
            updateLanguage = updateSourceLanguage,
            textColor = textColor
        )
        ExchangeButton(tint = exchangeButtonTint) {
            val temp = sourceLanguage
            updateSourceLanguage(targetLanguage)
            updateTargetLanguage(temp)
        }
        LanguageSelect(
            Modifier.semantics {
                contentDescription = ResStrings.des_current_target_lang
            },
            language = targetLanguage,
            languages = enabledLanguages,
            updateLanguage = updateTargetLanguage,
            textColor = textColor
        )
    }
}

@Composable
internal fun LanguageSelect(
    modifier: Modifier = Modifier,
    language: Language,
    languages: List<Language>,
    updateLanguage: (Language) -> Unit,
    textColor: Color = Color.White
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    TextButton(
        modifier = modifier, onClick = {
            expanded = true
        }
    ) {
        Text(
            text = language.displayText,
            fontSize = 18.sp,
            fontWeight = FontWeight.W600,
            color = textColor
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach {
                DropdownMenuItem(onClick = {
                    updateLanguage(it)
                    expanded = false
                }, text = {
                    Text(it.displayText)
                })
            }
        }
    }
}

internal val DESTINATION_IMAGE_FOLDER = CacheManager.cacheDir.resolve("cropped_image").also { it.mkdirs() }