package com.funny.translation.translate.ui.long_text

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.DataHolder
import com.funny.translation.helper.LocalContext
import com.funny.translation.helper.LocalNavController
import com.funny.translation.helper.Log
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.formatBraceStyle
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.helper.safeSubstring
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.NavController
import com.funny.translation.kmp.readText
import com.funny.translation.kmp.rememberOpenFileLauncher
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.LocalActivityVM
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.ui.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.navigation.NavOptions
import java.util.UUID

private const val TAG = "LongTextTransScreen"

@Composable
fun LongTextTransScreen() {
    CommonPage(
        modifier = Modifier,
        title = ResStrings.long_text_trans,
    ) {
        val navController = LocalNavController.current
        val navigateToDetailPage = remember {
            { id: String?, text: String? ->
                navigateToLongTextTransDetailPage(navController, id, text)
            }
        }
        val context = LocalContext.current
        val filePickerLauncher = rememberOpenFileLauncher {
            it ?: return@rememberOpenFileLauncher
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val text = it.readText()
                    withContext(Dispatchers.Main) {
                        if (text.isNotBlank()) {
                            navigateToDetailPage(null, text)
                        } else {
                            context.toastOnUi(ResStrings.file_empty)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                context.toastOnUi(ResStrings.file_read_error)
            }
        }
        var clipboardText by rememberStateOf(value = ClipBoardUtil.read())
        var showClipboardCard by rememberStateOf(value = clipboardText.isNotBlank())
        val lifeCycleEvent = LocalActivityVM.current.activityLifecycleState

        LaunchedEffect(key1 = lifeCycleEvent) {
            lifeCycleEvent.collect {
                if (showClipboardCard) return@collect
                if (it == Lifecycle.Event.ON_RESUME) {
                    delay(500) // Android 高版本仅在获取到焦点后才能读取剪切板
                    clipboardText = ClipBoardUtil.read()
                    Log.d(
                        TAG,
                        "LongTextTransScreen: onResume, clipboardText = ${
                            clipboardText.safeSubstring(
                                0,
                                10
                            )
                        }"
                    )
                    showClipboardCard = clipboardText.isNotBlank()
                }
            }
        }

        AnimatedVisibility(visible = showClipboardCard) {
            ItemCard(
                icon = Icons.Default.ContentPaste,
                title = ResStrings.from_clipboard,
                description = clipboardText,
                onClick = {},
                wrapper = { content ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.elevatedCardColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                        content = content
                    )
                },
                extraContent = {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showClipboardCard = false }) {
                            Text(text = ResStrings.close)
                        }
                        // 创建任务
                        TextButton(onClick = {
                            navigateToDetailPage(null, clipboardText)
                        }) {
                            Text(text = ResStrings.create_task)
                        }
                    }
                }
            )
        }
        ItemCard(
            icon = Icons.Default.Edit,
            title = ResStrings.from_editable_text,
            description = ResStrings.from_editable_text_description,
            onClick = {
                navController.navigateToTextEdit(
                    TextEditorAction.NewDraft("")
                )
            }
        )
        ItemCard(
            icon = Icons.Default.FileOpen,
            title = ResStrings.from_file,
            description = ResStrings.from_file_description,
            onClick = {
                // 从文件，仅限于各类文本文件和 JSON 文件
                filePickerLauncher.launch(arrayOf("text/*", "application/json"))
            }
        )
        ItemCard(
            icon = Icons.Default.Drafts,
            title = ResStrings.drafts,
            description = ResStrings.drafts_description,
            onClick = {
                navController.navigate(TranslateScreen.DraftScreen.route)
            }
        )
        Spacer(modifier = Modifier.heightIn(12.dp))
        Text(
            text = ResStrings.view_all_trans_histories,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .clickable {
                    navController.navigate(TranslateScreen.LongTextTransListScreen.route)
                }
                .padding(4.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ItemCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: SimpleAction,
    wrapper: @Composable (content: @Composable ColumnScope.() -> Unit) -> Unit = { content ->
        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.outlinedCardColors(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(0.5f)),
            content = content
        )
    },
    extraContent: (@Composable () -> Unit)? = null
) {
    wrapper {
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp)
        )
        {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FixedSizeIcon(
                    imageVector = icon,
                    modifier = Modifier
                        .size(36.dp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
            Text(
                modifier = Modifier
                    .padding(top = 4.dp, start = 6.dp, bottom = 4.dp),
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalContentColor.current.copy(alpha = 0.8f),
                maxLines = if (extraContent != null) 2 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis
            )
            extraContent?.invoke()
        }

    }
}

//@Preview
@Composable
private fun CardPreview() {
    ItemCard(
        icon = Icons.Default.Edit,
        title = ResStrings.from_editable_text,
        description = ResStrings.from_editable_text_description,
        onClick = {}
    )
}

/**
 * 导航到长文本翻译详情页，如果 id 为 null，则为创建任务并导航过去；否则为导航到已有任务的详情页
 * @param navController NavController
 * @param id String?
 * @param text String?
 * @param navOptions NavOptions?
 */
internal fun navigateToLongTextTransDetailPage(
    navController: NavController,
    id: String?,
    text: String?,
    navOptions: NavOptions? = null
) {
    // 如果 id 为 null，则为创建任务并导航过去
    if (id == null) {
        val newId = UUID.randomUUID().toString()
        DataHolder.put(newId, text)
        navController.navigate(
            TranslateScreen.LongTextTransDetailScreen.route.formatBraceStyle("id" to newId),
            navOptions
        )
    } else {
        navController.navigate(
            TranslateScreen.LongTextTransDetailScreen.route.formatBraceStyle("id" to id),
            navOptions
        )
    }
}