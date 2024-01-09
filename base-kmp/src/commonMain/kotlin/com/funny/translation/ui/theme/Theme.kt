package com.funny.translation.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.kmp.base.strings.ResStrings


val LightColors = lightColorScheme(
    surfaceTint = md_theme_light_surfaceTint,
    onErrorContainer = md_theme_light_onErrorContainer,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    tertiary = md_theme_light_tertiary,
    error = md_theme_light_error,
    outline = md_theme_light_outline,
    onBackground = md_theme_light_onBackground,
    background = md_theme_light_background,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    surface = md_theme_light_surface,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    secondary = md_theme_light_secondary,
    inversePrimary = md_theme_light_inversePrimary,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    primary = md_theme_light_primary,
)


val DarkColors = darkColorScheme(
    surfaceTint = md_theme_dark_surfaceTint,
    onErrorContainer = md_theme_dark_onErrorContainer,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    tertiary = md_theme_dark_tertiary,
    error = md_theme_dark_error,
    outline = md_theme_dark_outline,
    onBackground = md_theme_dark_onBackground,
    background = md_theme_dark_background,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    surface = md_theme_dark_surface,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    secondary = md_theme_dark_secondary,
    inversePrimary = md_theme_dark_inversePrimary,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    primary = md_theme_dark_primary,
)

val SpringFestivalColorPalette = lightColorScheme(
    primary = MaterialColors.Red500,
    tertiary = MaterialColors.Red700,
    secondary = MaterialColors.RedA400,
    onSecondary = Color.White,
    primaryContainer = MaterialColors.Red200.copy(alpha = 0.7f),
    onPrimaryContainer = MaterialColors.RedA700
)

sealed class ThemeType(val id: Int) {
    object StaticDefault: ThemeType(-1)
    object DynamicNative : ThemeType(0)
    class DynamicFromImage(val color: Color) : ThemeType(1)
    class StaticFromColor(val color: Color): ThemeType(2)

    val isDynamic get() = this is DynamicNative || this is DynamicFromImage

    override fun toString(): String {
        return when(this){
            StaticDefault -> "StaticDefault"
            DynamicNative -> "DynamicNative"
            is DynamicFromImage -> "DynamicFromImage#${this.color}"
            is StaticFromColor -> "StaticFromColor${this.color}"
        }
    }

    companion object {
        val Saver = { themeType: ThemeType ->
            when(themeType){
                StaticDefault -> "-1#0"
                DynamicNative -> "0#0"
                is DynamicFromImage -> "1#${themeType.color.toArgb()}"
                is StaticFromColor -> "2#${themeType.color.toArgb()}"
            }
        }

        val Restorer = { str: String ->
            val (id, color) = str.split("#")
            when(id){
                "-1" -> StaticDefault
                "0" -> DynamicNative
                "1" -> DynamicFromImage(Color(color.toInt()))
                "2" -> StaticFromColor(Color(color.toInt()))
                else -> throw IllegalArgumentException("Unknown ThemeType: $str")
            }
        }
    }
}

enum class LightDarkMode(val desc: String) {
    Light(ResStrings.always_light), Dark(ResStrings.always_dark), System(ResStrings.follow_system);

    override fun toString(): String {
        return desc
    }
}

expect object ThemeConfig {
    val TAG: String
    val defaultThemeType: ThemeType
    val sThemeType: MutableState<ThemeType>
    val lightDarkMode: MutableState<LightDarkMode>
    fun updateThemeType(new: ThemeType)
    fun updateLightDarkMode(new: LightDarkMode)
}

@Composable
@ReadOnlyComposable
internal fun calcDark(): Boolean {
    val lightDarkMode by ThemeConfig.lightDarkMode
    return when(lightDarkMode) {
        LightDarkMode.Light -> false
        LightDarkMode.Dark -> true
        LightDarkMode.System -> isSystemInDarkTheme()
    }
}

// 由于系统的 isSystemInDarkTheme 在 Desktop 报错 java.lang.UnsatisfiedLinkError: 'int org.jetbrains.skiko.SystemTheme_awtKt.getCurrentSystemTheme()
// 自己实现一个

@ReadOnlyComposable
@Composable
expect fun isSystemInDarkTheme(): Boolean

@Composable
expect fun TransTheme(
    dark: Boolean = calcDark(),
    hideStatusBar: Boolean = false,
    content: @Composable () -> Unit
)

val ColorScheme.isLight: Boolean
    @Composable
    @ReadOnlyComposable
    get() = !calcDark()



