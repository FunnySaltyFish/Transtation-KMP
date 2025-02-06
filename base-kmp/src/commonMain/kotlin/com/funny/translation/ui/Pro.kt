package com.funny.translation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun TransProIcon(
    modifier: Modifier = Modifier,
    tint: Color = ProIconColor,
) {
    FixedSizeIcon(
        Icons.Filled.Verified,
        modifier = modifier,
        tint = tint,
        contentDescription = null,
    )
}

private val ProIconColor = Color(0xFFFFA500)