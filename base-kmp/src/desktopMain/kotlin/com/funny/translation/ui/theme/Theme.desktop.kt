package com.funny.translation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.funny.translation.AppConfig
import com.funny.translation.helper.DateUtils
import com.jthemedetecor.OsThemeDetector

@Composable
actual fun TransTheme(
    dark: Boolean,
    hideStatusBar: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (AppConfig.sSpringFestivalTheme.value && DateUtils.isSpringFestival)
            SpringFestivalColorPalette
        else when (ThemeConfig.sThemeType.value) {
            ThemeType.StaticDefault -> if (dark) DarkColors else LightColors
            ThemeType.DynamicNative -> null
            else -> null
        }

    when (ThemeConfig.sThemeType.value) {
        ThemeType.StaticDefault, ThemeType.DynamicNative -> {
            MaterialTheme(
                colorScheme = colorScheme ?: if (dark) DarkColors else LightColors,
                content = content
            )
        }
        is ThemeType.DynamicFromImage -> {
            MonetTheme(
                color = (ThemeConfig.sThemeType.value as ThemeType.DynamicFromImage).color,
                content = content
            )
        }
        is ThemeType.StaticFromColor -> {
            MonetTheme(
                color = (ThemeConfig.sThemeType.value as ThemeType.StaticFromColor).color,
                content = content
            )
        }
    }
}


private val detector = OsThemeDetector.getDetector()


@Composable
@ReadOnlyComposable
actual fun isSystemInDarkTheme(): Boolean = detector.isDark
actual fun supportDynamicTheme(): Boolean = false