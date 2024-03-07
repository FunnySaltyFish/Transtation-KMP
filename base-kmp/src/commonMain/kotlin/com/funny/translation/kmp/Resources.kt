package com.funny.translation.kmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.imageResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun painterDrawableRes(name: String, suffix: String = "png"): Painter {
    val res = if (name.contains('.')) name else "$name.$suffix"
    return painterResource("drawable/$res")
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun painterResource(resource: String): Painter {
    return BitmapPainter(imageResource(DrawableResource(resource)))
}