package com.funny.translation.translate.ui.long_text

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.funny.compose.ai.token.TokenCounter
import com.funny.translation.bean.EditablePrompt
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ui.long_text.components.TokenNum
import com.funny.translation.ui.FixedSizeIcon

@Composable
internal fun ColumnScope.PromptPart(
    initialPrompt: EditablePrompt,
    tokenCounter: TokenCounter,
    onPrefixUpdate: (String) -> Unit,
    resetPromptAction: SimpleAction
) {
    var prefix by rememberStateOf(initialPrompt.prefix)
    LaunchedEffect(key1 = initialPrompt) {
        if (initialPrompt.prefix != prefix) prefix = initialPrompt.prefix
    }
    Category(
        title = ResStrings.task_prompt,
        helpText = ResStrings.task_prompt_help,
        expandable = true,
        extraRowContent = {
            FixedSizeIcon(Icons.Filled.Replay, contentDescription = "reset", modifier = Modifier
                .size(16.dp)
                .clickable(onClick = resetPromptAction))
            TokenNum(modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End), tokenCounter = tokenCounter, text = prefix + initialPrompt.suffix)
        }
    ) { expanded: Boolean ->
        Column {
            var textLayoutResult: TextLayoutResult? by rememberStateOf(null)
            BasicTextField(
                value = prefix,
                onValueChange = {
                    prefix = it
                    onPrefixUpdate(it)
                },
                onTextLayout = { result ->
                    textLayoutResult = result
                }
            ) { innerTextField ->
                innerTextField()
            }
            // 根据 textLayoutResult 计算出 prefix 的位置，
            // 实现 suffix 刚刚好接在 prefix 后面
            val maxLines by animateIntAsState(targetValue = if (expanded) 10 else 2, label = "PromptSuffix")
            Text(
                text = initialPrompt.suffix,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .animateContentSize(),
                style = LocalTextStyle.current.copy(color = Color.Gray),
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}