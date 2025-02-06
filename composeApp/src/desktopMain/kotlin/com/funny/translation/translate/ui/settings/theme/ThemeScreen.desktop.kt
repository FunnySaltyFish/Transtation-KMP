package com.funny.translation.translate.ui.settings.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.funny.translation.kmp.KMPContext
import com.funny.translation.ui.theme.LightColors
import com.materialkolor.ktx.themeColors
import java.io.File
import javax.imageio.ImageIO

actual fun getColorFromImageUri(
    context: KMPContext,
    uri: String
): Color? {
    val file = File(uri.removePrefix("file://"))
    val image = ImageIO.read(file)
    val imageBitmap: ImageBitmap = image.toComposeImageBitmap()
    val color = imageBitmap.themeColors(fallback = LightColors.primary).firstOrNull()
    return color
}