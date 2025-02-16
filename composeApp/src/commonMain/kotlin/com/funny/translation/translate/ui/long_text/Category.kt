package com.funny.translation.translate.ui.long_text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.translation.helper.rememberStateOf
import com.funny.translation.translate.ui.widget.ExpandMoreButton
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.RichTooltipCloseButton


/**
 * 可展开的 Category
 * @receiver ColumnScope
 * @param title String
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Category(
    title: String,
    helpText: String = "helpText",
    expandable: Boolean = true,
    defaultExpand: Boolean = false,
    extraRowContent: @Composable() (RowScope.() -> Unit)? = null,
    content: @Composable (expanded: Boolean) -> Unit,
) {
    Column {
        var expand by rememberStateOf(value = defaultExpand)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            val tooltipState = rememberTooltipState(isPersistent = true)
            TooltipBox(
                positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                state = tooltipState,
                tooltip = {
                    RichTooltip(
                        text = {
                            Text(text = helpText, style = MaterialTheme.typography.bodySmall)
                        },
                        action = {
                            RichTooltipCloseButton(tooltipState)
                        }
                    )
                }
            ) {
                FixedSizeIcon(
                    Icons.Default.QuestionMark, "", tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(14.dp)
                        .offset(4.dp, (-0).dp)
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                if (extraRowContent != null) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.labelSmall
                    ) {
                        extraRowContent()
                    }
                }
            }
            if (expandable) {
                ExpandMoreButton(modifier = Modifier, expand = expand, onClick = {
                    expand = !expand
                })
            }
        }
        content(expand)
    }
}