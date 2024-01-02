package com.funny.translation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.AppConfig
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.DateUtils
import com.funny.translation.helper.DeviceUtils
import com.funny.translation.helper.Log
import com.funny.translation.helper.toastOnUi
import com.funny.translation.kmp.appCtx

actual object ThemeConfig {
    actual const val TAG = "ThemeConfig"
    actual val defaultThemeType: ThemeType = ThemeType.StaticDefault


    actual val sThemeType: MutableState<ThemeType> =
        mutableDataSaverStateOf(DataSaverUtils, "theme_type", defaultThemeType)
    actual val lightDarkMode: MutableState<LightDarkMode> =
        mutableDataSaverStateOf(DataSaverUtils, "light_dark_mode", LightDarkMode.System)

    actual fun updateThemeType(new: ThemeType) {
        if (new == ThemeType.DynamicNative) {
            appCtx.toastOnUi("Android 12 以上才支持动态主题哦")
            return
        }

        // 如果是 FromXXX，必须 64 位才行
        if (
            (new is ThemeType.DynamicFromImage || new is ThemeType.StaticFromColor)
            && !DeviceUtils.is64Bit()
        ) {
            appCtx.toastOnUi("抱歉，由于库底层限制，仅 64 位机型才支持自定义取色")
            return
        }

        sThemeType.value = new
        Log.d(TAG, "updateThemeType: $new")
    }

    actual fun updateLightDarkMode(new: LightDarkMode) {
        lightDarkMode.value = new
    }
}

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
                // Desktop 不支持
                return@run null
            }
            else -> null
        }

    MaterialTheme(
        colorScheme = colorScheme ?: if (dark) DarkColors else LightColors,
        content = content
    )
}