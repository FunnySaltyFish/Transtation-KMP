package com.funny.translation.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun SpacerWidth(width: Dp) {
    androidx.compose.foundation.layout.Spacer(
        Modifier
            .width(width)
    )
}

@Composable
fun SpacerHeight(height: Dp) {
    androidx.compose.foundation.layout.Spacer(
        Modifier
            .height(height)
    )
}