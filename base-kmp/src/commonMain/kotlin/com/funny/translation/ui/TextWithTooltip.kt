package com.funny.translation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.helper.SimpleAction
import com.funny.translation.kmp.ifThen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TwoTextWithTooltip(
    modifier: Modifier = Modifier,
    text: String,
    text1: String,
    text1Desc: String,
    text2: String,
    text2Desc: String,
    supportingText: String,
    contentColor: Color,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    onClick: SimpleAction? = null,
) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        state = tooltipState,
        tooltip =  {
            RichTooltip(
                action = {
                    RichTooltipCloseButton(tooltipState)
                }
            ) {
                Column {
                    FlowRow (
                        modifier = Modifier,
                    ) {
                        // ai_point + vip_free_ai_point
                        val textStyle = MaterialTheme.typography.labelLarge
                        SingleBadgedText(
                            value = text1,
                            description = text1Desc,
                            style = textStyle
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = "+",
                            style = textStyle
                        )
                        SingleBadgedText(
                            value = text2,
                            description = text2Desc,
                            style = textStyle
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    MarkdownText(
                        markdown = supportingText,
                        fontSize = 10.sp,
                        color = LocalContentColor.current.copy(alpha = 0.8f)
                    )
                }
            }
        }
    ) {
        Row(
            Modifier.ifThen(onClick != null, then = { clickable(onClick = onClick!!) } ).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier,
                text = text,
                style = style,
                color = contentColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            FixedSizeIcon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Outlined.Info,
                contentDescription = "提示",
                tint = contentColor,
            )
        }
    }
}

@Composable
private fun RowScope.SingleBadgedText(
    value: String,
    description: String,
    style: TextStyle = MaterialTheme.typography.labelMedium
) {
    Text(
        text = value,
        style = style
    )
    Spacer(modifier = Modifier.width(4.dp))
    Badge {
        Text(description)
    }
}