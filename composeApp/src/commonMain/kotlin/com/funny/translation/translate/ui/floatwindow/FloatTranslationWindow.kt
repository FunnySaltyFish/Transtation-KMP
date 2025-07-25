package com.funny.translation.translate.ui.floatwindow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.AppConfig
import com.funny.translation.helper.Log
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.rememberDerivedStateOf
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.Language
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.ui.main.MainViewModel
import com.funny.translation.translate.ui.main.SpeakButton
import com.funny.translation.translate.ui.main.components.ChildrenFixedSizeRow
import com.funny.translation.translate.ui.main.components.LanguageListMenu
import com.funny.translation.translate.ui.main.components.TextTransResultItem
import com.funny.translation.translate.ui.widget.ExchangeButton
import com.funny.translation.translate.utils.EngineManager
import com.funny.translation.ui.SpacerHeight

@Composable
fun FloatingTranslationWindow(
    viewModel: MainViewModel,
    onClose: () -> Unit,
    onOpenApp: (vm: MainViewModel) -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onTapInput: SimpleAction = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            // Top bar with language selectors and close button
            FloatingWindowTopBar(
                sourceLanguage = viewModel.sourceLanguage,
                targetLanguage = viewModel.targetLanguage,
                updateSourceLanguage = viewModel::updateSourceLanguage,
                updateTargetLanguage = viewModel::updateTargetLanguage,
                onClose = onClose
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Input field and action buttons
            FloatingWindowInputField(
                value = viewModel.translateText,
                onValueChange = viewModel::updateTranslateText,
                onClear = { viewModel.updateTranslateText("") },
                sourceLanguage = viewModel.sourceLanguage,
                onTranslate = viewModel::translate,
                onStopTranslate = viewModel::cancel,
                translating = viewModel.translating,
                interactionSource = interactionSource,
                onTapInput = onTapInput,
                onEngineSelected = { new ->
                    viewModel.selectedEngines.clear()
                    viewModel.addSelectedEngines(new)
                }
            )

            val result by rememberDerivedStateOf {
                viewModel.resultList.firstOrNull()
            }
            if (result != null) {
                SpacerHeight(8.dp)
                TextTransResultItem(
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(start = 8.dp, end = 0.dp, bottom = 4.dp, top = 4.dp)
                        .verticalScroll(rememberScrollState()),
                    result = result!!,
                    doFavorite = viewModel::doFavorite,
                    stopTranslateAction = viewModel::stopOneJob,
                    smartTransEnabled = AppConfig.sAITransExplain.value,
                )
                SpacerHeight(8.dp)
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy((-8).dp),
                ) {
                    FloatWindowShowEngineSelectButton()
                    IconButton(onClick = {
                        onOpenApp(viewModel)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open App",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun FloatingWindowTopBar(
    sourceLanguage: Language,
    targetLanguage: Language,
    updateSourceLanguage: (Language) -> Unit,
    updateTargetLanguage: (Language) -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChildrenFixedSizeRow(
            modifier = Modifier.weight(1f),
            elementsPadding = 16.dp,
            left = {
                LanguageSelector(
                    Modifier.semantics {
                        contentDescription = ResStrings.des_current_source_lang
                    },
                    language = sourceLanguage,
                    updateLanguage = updateSourceLanguage
                )
            }, center = {
                ExchangeButton(tint = LocalContentColor.current) {
                    val temp = sourceLanguage
                    updateSourceLanguage(targetLanguage)
                    updateTargetLanguage(temp)
                }
            }, right = {
                LanguageSelector(
                    Modifier.semantics {
                        contentDescription = ResStrings.des_current_target_lang
                    },
                    language = targetLanguage,
                    updateLanguage = updateTargetLanguage
                )
            }
        )

        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun LanguageSelector(
    modifier: Modifier = Modifier,
    language: Language,
    updateLanguage: (Language) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            text = language.displayText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp)
        )

        LanguageListMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            updateLanguage = updateLanguage
        )
    }
}

@Composable
private fun FloatingWindowInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    sourceLanguage: Language,
    onTranslate: () -> Unit,
    onStopTranslate: () -> Unit,
    translating: Boolean,
    interactionSource: MutableInteractionSource,
    onTapInput: SimpleAction,
    onEngineSelected: (new: TranslationEngine) -> Unit,
) {
    val selectEngine by EngineManager.floatWindowTranslateEngineStateFlow.collectAsState()

    LaunchedEffect(selectEngine) {
        onEngineSelected(selectEngine)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Input text field
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    ResStrings.translate_engine_hint.format(selectEngine.name),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged {
                    Log.d("FloatingWindowInputField", "onFocusChanged: ${it.isFocused}")
                    if (it.isFocused) {
                        onTapInput()
                    }
                },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            ),
            maxLines = 3,
            trailingIcon = {
                if (value.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy((-12).dp)
                    ) {
                        val tint = MaterialTheme.colorScheme.onSurfaceVariant
                        SpeakButton(
                            modifier = Modifier.minimumInteractiveComponentSize(),
                            text = value.trim(),
                            language = sourceLanguage,
                            tint = tint
                        )
                        IconButton(
                            onClick = {
                                if (translating) onStopTranslate() else onTranslate()
                            },
                        ) {
                            if (!translating) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Translate",
                                    tint = tint
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop Translate",
                                    tint = tint
                                )
                            }
                        }
                        IconButton(
                            onClick = onClear,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    FloatWindowShowEngineSelectButton()
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(8.dp),
            interactionSource = interactionSource,
        )
    }
}

@Composable
expect fun EngineSelectDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSelect: (TranslationEngine) -> Unit
)

@Composable
internal fun FloatWindowShowEngineSelectButton() {
    var showEngineSelect by remember { mutableStateOf(false) }
    IconButton(
        onClick = { showEngineSelect = true },
        modifier = Modifier.minimumInteractiveComponentSize()
            .semantics { contentDescription = ResStrings.engine_select }
    ) {
        Icon(
            imageVector = Icons.Default.Translate,
            contentDescription = "Select Engine",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    EngineSelectDialog(
        show = showEngineSelect,
        onDismiss = { showEngineSelect = false },
        onSelect = {
            val old = EngineManager.floatWindowTranslateEngineStateFlow.value
            if (old == it) {
                return@EngineSelectDialog
            }
            EngineManager.floatWindowTranslateEngineStateFlow.value = it
            Log.d("FloatingWindowInputField", "Selected engine: $it")
            showEngineSelect = false
        }
    )
}