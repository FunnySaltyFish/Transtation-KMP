package com.funny.translation.helper

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/**
 * 获得忽略 fontScale 非线性缩放，拿到真实的 dp
 */
// context(density: Density)
fun TextUnit.toRealDp(density: Density) = Dp(this.value * density.fontScale)