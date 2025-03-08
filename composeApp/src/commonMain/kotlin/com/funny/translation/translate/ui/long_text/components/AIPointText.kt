package com.funny.translation.translate.ui.long_text.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.funny.translation.AppConfig
import com.funny.translation.helper.LocalNavController
import com.funny.translation.helper.formatBraceStyle
import com.funny.translation.strings.ResStrings
import com.funny.translation.translate.bean.AI_TEXT_POINT
import com.funny.translation.translate.navigateSingleTop
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.ui.TwoTextWithTooltip

@Composable
fun AIPointText() {
    val user = AppConfig.userInfo.value
    val value = user.ai_text_point
    val navController = LocalNavController.current
    TwoTextWithTooltip(
        modifier = Modifier,
        text = value.toString(),
        text1 = user.ai_point.toString(),
        text1Desc = ResStrings.self_bought,
        text2 = user.vip_free_ai_point.toString(),
        text2Desc = ResStrings.vip_give,
        supportingText = ResStrings.vip_free_point_tip,
        contentColor = LocalContentColor.current,
        onClick = {
            navController.navigateSingleTop(
                route = TranslateScreen.BuyAIPointScreen.route.formatBraceStyle(
                    "planName" to AI_TEXT_POINT
                )
            )
        }
    )
}