package com.funny.translation.translate.ui.long_text.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.AppConfig
import com.funny.translation.helper.LocalNavController
import com.funny.translation.helper.formatBraceStyle
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.bean.AI_TEXT_POINT
import com.funny.translation.translate.navigateSingleTop
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.widget.RichTooltipCloseButton
import com.funny.translation.ui.FixedSizeIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIPointText() {
    val user = AppConfig.userInfo.value
    val value = user.ai_text_point
    val navController = LocalNavController.current
    val tooltipState = rememberTooltipState(isPersistent = true)
    TooltipBox(
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        state = tooltipState,
        tooltip =  {
            RichTooltip(
                action = {
                    RichTooltipCloseButton(tooltipState)
                }
            ) {
                Column {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ai_point + vip_free_ai_point
                        val style = MaterialTheme.typography.labelLarge
                        SingleBadgedText(
                            value = user.ai_text_point.toString(),
                            description = ResStrings.self_bought,
                            style = style
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = "+",
                            style = style
                        )
                        SingleBadgedText(
                            value = user.vip_free_ai_point.toString(),
                            description = ResStrings.vip_give,
                            style = style
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = ResStrings.vip_free_point_tip,
                        fontSize = 10.sp,
                        color = LocalContentColor.current.copy(alpha = 0.8f)
                    )
                }
            }
        }
    ) {
        Row(
            Modifier.clickable {
                navController.navigateSingleTop(
                    route = TranslateScreen.BuyAIPointScreen.route.formatBraceStyle(
                        "planName" to AI_TEXT_POINT
                    )
                )
            }.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier,
                text = value.toString()
            )
            Spacer(modifier = Modifier.width(4.dp))
            FixedSizeIcon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Outlined.Info,
                contentDescription = "提示",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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