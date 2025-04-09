package com.funny.translation.ui.theme

import android.graphics.Color
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.funny.translation.AppConfig
import com.funny.translation.helper.DateUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.LocalKMPContext
import com.funny.translation.kmp.appCtx

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
            ThemeType.DynamicNative -> run {
                // 小于 Android 12 直接跳过
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@run null
                val context = LocalKMPContext.current
                if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            else -> null
        }

    val mContent = @Composable {
        // SystemBarSettings(hideStatusBar)
        val context = LocalContext.current as? ComponentActivity
        if (context != null) {
            val c = MaterialTheme.colorScheme.primaryContainer.toArgb()
            DisposableEffect(dark) {
                context.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                    ) { dark },
                    navigationBarStyle =
                    if (dark) SystemBarStyle.dark(transparent)
                    else SystemBarStyle.light(transparent, c),
                )
                onDispose {}
            }
        }
        content()
    }

    when (ThemeConfig.sThemeType.value) {
        ThemeType.StaticDefault, ThemeType.DynamicNative -> {
            MaterialTheme(
                colorScheme = colorScheme ?: if (dark) DarkColors else LightColors,
                content = mContent
            )
        }
        is ThemeType.DynamicFromImage -> {
            MonetTheme(
                color = (ThemeConfig.sThemeType.value as ThemeType.DynamicFromImage).color,
                content = mContent
            )
        }
        is ThemeType.StaticFromColor -> {
            MonetTheme(
                color = (ThemeConfig.sThemeType.value as ThemeType.StaticFromColor).color,
                content = mContent
            )
        }
    }
}

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = android.graphics.Color.argb(0x00, 0x00, 0x00, 0x00)

private val transparent = android.graphics.Color.argb(0x00, 0x00, 0x00, 0x00)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)

@Composable
@ReadOnlyComposable
actual fun isSystemInDarkTheme() = androidx.compose.foundation.isSystemInDarkTheme()
actual fun supportDynamicTheme(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        appCtx.toastOnUi("Android 12 以上才支持动态主题哦")
        return false
    }
    return true
}