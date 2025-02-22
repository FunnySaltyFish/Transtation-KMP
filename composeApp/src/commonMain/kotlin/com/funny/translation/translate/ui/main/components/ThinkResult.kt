package com.funny.translation.translate.ui.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.helper.rememberSaveableStateOf
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.ThinkingStage
import com.funny.translation.translate.TranslationResult
import com.funny.translation.ui.SpacerWidth

@Composable
fun ThinkResult(
    modifier: Modifier = Modifier,
    result: TranslationResult
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialColors.Grey200, contentColor = LocalTextStyle.current.color.copy(alpha = 0.85f)),
    ) {
        var expandThink by rememberSaveableStateOf(true)
        Row(
            modifier = Modifier.fillMaxWidth().clickable {
                expandThink = !expandThink
            }.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (result.thinkStage) {
                    ThinkingStage.THINKING -> {
                        SpacerWidth(8.dp)
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = ResStrings.thinking_in_progress,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    ThinkingStage.FINISH -> {
                        Icon(
                            imageVector = Icons.Rounded.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).padding(start = 2.dp),
                        )
                        Text(
                            text = ResStrings.thinking_completed.format(result.thinkTime),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    else -> {}
                }
            }

            Icon(
                imageVector = if (expandThink) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = ResStrings.expand_think_content,
                modifier = Modifier.size(24.dp),
            )
        }

        if (expandThink && result.think != "") {
            Text(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                text = result.think.trim(),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

//@Preview
@Composable
fun ThinkResultPreview() {
    ThinkResult(result = TranslationResult().apply {
        thinkStage = ThinkingStage.FINISH
        thinkTime = 12.3f
        think = "好的，用户的意思是整理一下下面的代码，并在主要的内容上方新增一个“深度思考块儿”，使用 result.think 和 result.think time 包含一个状态（转圈、已深度思考（用时xx秒））、展开/伸缩按钮，文字使用xml并同时给出中英双语，使用 ResString.xxx 访问。整体UI美观大方，文字偏小，可以与其他区域区分开，使用 M3 组件"
    })
}