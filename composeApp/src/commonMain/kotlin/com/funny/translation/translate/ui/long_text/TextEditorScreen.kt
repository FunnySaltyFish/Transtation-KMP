package com.funny.translation.translate.ui.long_text

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.funny.compose.ai.token.TokenCounters
import com.funny.jetsetting.core.ui.SimpleDialog
import com.funny.translation.helper.DataHolder
import com.funny.translation.helper.LocalNavController
import com.funny.translation.helper.Log
import com.funny.translation.helper.TimeUtils
import com.funny.translation.helper.formatQueryStyle
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.kmp.NavController
import com.funny.translation.kmp.kmpImeNestedScroll
import com.funny.translation.kmp.navOptions
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.database.Draft
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.database.draftDao
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.long_text.components.TokenNumRow
import com.funny.translation.ui.CommonNavBackIcon
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.navigation.BackHandler
import java.net.URLDecoder
import java.net.URLEncoder

internal const val KEY_EDITED_SOURCE_TEXT_KEY = "KEY_EDITED_SOURCE_TEXT"

// 用于在 TextEditorScreen 中传递数据
// 由于 text 可能非常长，此处不存储 text，而是存储 textKey
// 数据从 DataHolder 中取出
sealed class TextEditorAction(val textKey: String, val tokenCounterId: String = "default") {
    class NewDraft(textKey: String) : TextEditorAction(textKey) {
        override fun toString(): String {
            return "NewDraft$SEPARATOR${URLEncoder.encode(textKey, "UTF-8")}"
        }
    }
    class UpdateDraft(val draftId: Int, textKey: String) : TextEditorAction(textKey) {
        override fun toString(): String {
            return "UpdateDraft$SEPARATOR$draftId$SEPARATOR${URLEncoder.encode(textKey, "UTF-8")}"
        }
    }
    class UpdateSourceText(textKey: String, tokenCounterId: String) : TextEditorAction(textKey, tokenCounterId) {
        override fun toString(): String {
            return "UpdateSourceText$SEPARATOR${URLEncoder.encode(textKey, "UTF-8")}$SEPARATOR$tokenCounterId"
        }
    }

    fun putToDataHolder(content: String) {
        DataHolder.put(textKey, content)
    }

    fun getFromDataHolder(): String {
        return DataHolder.get(textKey) ?: ""
    }

    companion object {
        private const val SEPARATOR = " "
        fun fromString(string: String): TextEditorAction {
            return when {
                string.startsWith("NewDraft") -> {
                    val split = string.split(SEPARATOR)
                    NewDraft(split[1].let { URLDecoder.decode(it, "UTF-8") })
                }
                string.startsWith("UpdateDraft") -> {
                    val split = string.split(SEPARATOR)
                    UpdateDraft(split[1].toInt(), split[2].let { URLDecoder.decode(it, "UTF-8")  })
                }
                string.startsWith("UpdateSourceText") -> {
                    val split = string.split(SEPARATOR)
                    UpdateSourceText(split[1].let { URLDecoder.decode(it, "UTF-8") }, split[2])
                }
                else -> throw IllegalArgumentException("Unknown TextEditorAction: $string")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextEditorScreen(
    action: TextEditorAction?
) {
    if (action == null) {
        Text(
            text = ResStrings.illegal_action,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
        return
    }
    val showDialog = rememberStateOf(value = false)
    val navController = LocalNavController.current
    val initialText = remember(action) { action.getFromDataHolder() }
    var textFieldValue by rememberStateOf(value = TextFieldValue(
        initialText, TextRange(initialText.length)
    ))
    val text by remember { derivedStateOf { textFieldValue.text } }
    val textEmpty by remember { derivedStateOf { textFieldValue.text == "" } }
    val tokenCounter = remember(action) { TokenCounters.findById(action.tokenCounterId) }

    SimpleDialog(
        openDialogState = showDialog,
        title = ResStrings.tip,
        message = when (action) {
            is TextEditorAction.NewDraft -> ResStrings.save_draft
            is TextEditorAction.UpdateDraft -> ResStrings.update_draft
            is TextEditorAction.UpdateSourceText -> ResStrings.update_source_text
        },
        confirmButtonText = ResStrings.save,
        confirmButtonAction = {
            // 保存或者更新草稿
            when (action) {
                is TextEditorAction.NewDraft, is TextEditorAction.UpdateDraft -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        appDB.draftDao.upsert(
                            Draft(content = textFieldValue.text, remark = TimeUtils.getNowStr(), id = (action as? TextEditorAction.UpdateDraft)?.draftId ?: 0)
                        )
                        withContext(Dispatchers.Main) {
                            navController.popBackStack()
                        }
                    }
                }
                is TextEditorAction.UpdateSourceText -> {
                    action.putToDataHolder(text)
                    // TODO 改成 navigateForResult 实现
                    navController.goBackWith(action.textKey)
                }
            }
        },
        dismissButtonText = ResStrings.exit,
        dismissButtonAction = {
            navController.popBackStack()
        }
    )

    val goBackAction = remember {
        {
            if (text == "" || text == initialText) {
                navController.popBackStack()
            } else {
                showDialog.value = true
            }
        }
    }

    BackHandler(!textEmpty) {
        goBackAction()
    }

    CommonPage(
        navigationIcon = {
            CommonNavBackIcon {
                goBackAction()
            }
        },
        actions = {
            TokenNumRow(tokenCounter = tokenCounter, text = text)
            if (action is TextEditorAction.NewDraft || action is TextEditorAction.UpdateDraft) {
                AnimatedVisibility(visible = !textEmpty) {
                    IconButton(onClick = {
                        navigateToLongTextTransDetailPage(
                            navController = navController,
                            id = null,
                            text = text,
                            navOptions = navOptions {
                                popUpTo(TranslateScreen.LongTextTransScreen.route)
                            })
                    }) {
                        FixedSizeIcon(Icons.Default.PlayArrow, contentDescription = "Start")
                    }
                }
            }
        }
    ) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(key1 = focusRequester) {
            delay(500)
            focusRequester.requestFocus()
        }
        val textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
        Box(modifier = Modifier.kmpImeNestedScroll()) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .imePadding()
                    .focusable(true)
                    .focusRequester(focusRequester),
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                textStyle = textStyle,
            ) {
                if (textEmpty) {
                    Text(
                        text = ResStrings.input_text_hint,
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                it()
            }
        }
    }
}

internal fun NavController.navigateToTextEdit(
    action: TextEditorAction,
    onResult: (String) -> Unit = {}
) {
    CoroutineScope(Dispatchers.Main.immediate).launch {
        val result = navigateForResult(
            TranslateScreen.TextEditorScreen.route.formatQueryStyle(
                "action" to action.toString()
            ).also {
                Log.d("TextEditorScreen", "navigateToTextEdit with route = $it")
            }
        )
        Log.d("TextEditorScreen", "navigateToTextEdit with result = $result")
        if (result != null) {
            onResult(result as String)
        }
    }
}
