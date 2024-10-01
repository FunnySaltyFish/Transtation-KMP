package com.funny.translation.kmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.decodeToImageVector

@OptIn(ExperimentalResourceApi::class)
@Composable
fun painterDrawableRes(name: String, suffix: String = "png"): Painter {
    val res = if (name.contains('.')) name else "$name.$suffix"
    return painterResource("drawable/$res")
}

// copied from https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources-usage.html#what-s-next
@Composable
internal fun painterResource(
    resourcePath: String
): Painter = when (resourcePath.substringAfterLast(".")) {
    // "svg" -> rememberSvgResource(resourcePath)
    "xml" -> rememberVectorXmlResource(resourcePath)
    else -> rememberBitmapResource(resourcePath)
}

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun rememberBitmapResource(path: String): Painter {
    return remember(path) { BitmapPainter(readResourceBytes(path).decodeToImageBitmap()) }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun rememberVectorXmlResource(path: String): Painter {
    val density = LocalDensity.current
    val imageVector = remember(density, path) { readResourceBytes(path).decodeToImageVector(density) }
    return rememberVectorPainter(imageVector)
}

//@OptIn(ExperimentalResourceApi::class)
//@Composable
//internal fun rememberSvgResource(path: String): Painter {
//    val density = LocalDensity.current
//    return remember(density, path) { readResourceBytes(path).decodeToSvgPainter(density) }
//}

private object ResourceLoader
private fun readResourceBytes(resourcePath: String) =
    ResourceLoader.javaClass.classLoader.getResourceAsStream(resourcePath)?.readAllBytes() ?: byteArrayOf()

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