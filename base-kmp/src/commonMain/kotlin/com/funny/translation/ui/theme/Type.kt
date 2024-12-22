package com.funny.translation.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
        /*
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)

val Typography.hint @Composable get() = TextStyle.Default.copy(
    color = LocalContentColor.current.copy(alpha = 0.8f),
    fontSize = 10.sp
)