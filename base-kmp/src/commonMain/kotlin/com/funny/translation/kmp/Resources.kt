package com.funny.translation.kmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun painterDrawableRes(name: String, suffix: String = "png"): Painter {
    val res = if (name.contains('.')) name else "$name.$suffix"
    return painterResource("drawable/$res")
}

@OptIn(ExperimentalResourceApi::class, InternalResourceApi::class)
@Composable
fun painterResource(resource: String): Painter {
    return org.jetbrains.compose.resources.painterResource(DrawableResource(path = resource))
}


///**
// * Adapted from v1.6.2
// * Creates an [DrawableResource] object with the specified path.
// *
// * @param path The path of the drawable resource.
// * @return An [DrawableResource] object.
// */
//@OptIn(InternalResourceApi::class)
//@ExperimentalResourceApi
//fun DrawableResource(path: String): DrawableResource = DrawableResource(
//    id = "DrawableResource:$path",
//    items = setOf(ResourceItem(emptySet(), path, -1, -1))
//)