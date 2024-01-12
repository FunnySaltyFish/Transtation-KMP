package com.funny.translation.kmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun painterDrawableRes(name: String, suffix: String = "png"): Painter {
    val res = if (name.contains('.')) name else "$name.$suffix"
    return painterResource("drawable/$res")
}