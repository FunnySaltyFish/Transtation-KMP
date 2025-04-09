package com.funny.translation.translate.ui.main.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.funny.translation.strings.ResStrings
import com.funny.translation.ui.TwoTextWithTooltip

@Composable
fun CostIndicator(
    modifier: Modifier = Modifier,
    selectingPromptCost: String,
    actualCost: String,
    totalCost: String,
    supportingString: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    TwoTextWithTooltip(
        modifier = modifier,
        text = totalCost,
        text1 = selectingPromptCost,
        text1Desc = ResStrings.selecting_prompt_cost,
        text2 = actualCost,
        text2Desc = ResStrings.actual_cost,
        style = LocalTextStyle.current.copy(
            fontSize = 12.sp,
        ),
        supportingText = supportingString,
        contentColor = color
    )
}