package com.funny.translation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import com.jthemedetecor.OsThemeDetector

@Composable
actual fun TransTheme(
    dark: Boolean,
    hideStatusBar: Boolean,
    content: @Composable () -> Unit
) {

    val colorScheme = remember(ThemeConfig.lightDarkMode.value, ThemeConfig.sThemeType.value) {
        when (ThemeConfig.sThemeType.value) {
            ThemeType.StaticDefault -> if (dark) DarkColors else LightColors
            ThemeType.DynamicNative -> run {
                // Desktop 不支持
                return@run null
            }
            else -> null
        }
    }

    MaterialTheme(
        colorScheme = colorScheme ?: if (dark) DarkColors else LightColors,
        content = content
    )
}

private val detector = OsThemeDetector.getDetector()


@Composable
@ReadOnlyComposable
actual fun isSystemInDarkTheme(): Boolean = detector.isDark
actual fun supportDynamicTheme(): Boolean = false