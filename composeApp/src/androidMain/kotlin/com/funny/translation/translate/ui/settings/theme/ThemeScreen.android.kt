package com.funny.translation.translate.ui.settings.theme

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import com.funny.translation.helper.BitmapUtil
import com.funny.translation.kmp.KMPContext
import com.funny.translation.ui.theme.LightColors
import com.materialkolor.ktx.themeColors

actual fun getColorFromImageUri(
    context: KMPContext,
    uri: String
): Color? {
    val bytes = BitmapUtil.getBitmapFromUri(context, 400, 600, 1024*1024, uri)
    bytes ?: return null
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val imageBitmap = bitmap.asImageBitmap()
    val color = imageBitmap.themeColors(fallback = LightColors.primary).firstOrNull()
    return color
}