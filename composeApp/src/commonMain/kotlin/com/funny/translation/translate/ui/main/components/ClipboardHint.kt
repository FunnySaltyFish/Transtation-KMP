package com.funny.translation.translate.ui.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.Log
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.translate.LocalActivityVM
import kotlinx.coroutines.delay

private val shownClipboardTexts = hashSetOf<String>()

// 主屏幕剪切板提示组件
@Composable
internal fun ClipboardHint(
    modifier: Modifier = Modifier,
    translateByClipboardText: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var clipboardText by rememberStateOf("")

    val activityVM = LocalActivityVM.current
    LaunchedEffect(key1 = activityVM) {
        activityVM.activityLifecycleState.collect {
            when (it) {
                Lifecycle.Event.ON_RESUME -> {
                    delay(800)
                    clipboardText = ClipBoardUtil.read()
                    Log.d("ClipBoardHint", "text: $clipboardText")
                    if (clipboardText.isNotEmpty() && clipboardText !in shownClipboardTexts) {
                        showDialog = true
                        delay(3000)
                        showDialog = false
                        shownClipboardTexts.add(clipboardText)
                    }
                }

                else -> Unit
            }
        }
    }

    AnimatedVisibility(
        visible = showDialog,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp)
                .clickable {
                    showDialog = false
                    translateByClipboardText(clipboardText)
                    shownClipboardTexts.add(clipboardText)
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "剪切板",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "剪切板内容",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = clipboardText,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}