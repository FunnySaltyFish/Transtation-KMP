package com.funny.translation.kmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance

// SharedCode
// copied from accompanist
@Stable
interface SystemUiController {
    var systemBarsBehavior: Int
    var isStatusBarVisible: Boolean
    var isNavigationBarVisible: Boolean
    var isSystemBarsVisible: Boolean
        get() = isNavigationBarVisible && isStatusBarVisible
        set(value) {
            isStatusBarVisible = value
            isNavigationBarVisible = value
        }

    fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        transformColorForLightContent: (Color) -> Color = { it }
    )

    fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        navigationBarContrastEnforced: Boolean = true,
        transformColorForLightContent: (Color) -> Color = { it }
    )

    var statusBarDarkContentEnabled: Boolean
    var navigationBarDarkContentEnabled: Boolean

    public var systemBarsDarkContentEnabled: Boolean
        get() = statusBarDarkContentEnabled && navigationBarDarkContentEnabled
        set(value) {
            statusBarDarkContentEnabled = value
            navigationBarDarkContentEnabled = value
        }

    var isNavigationBarContrastEnforced: Boolean

    public fun setSystemBarsColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        isNavigationBarContrastEnforced: Boolean = true,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    ) {
        setStatusBarColor(color, darkIcons, transformColorForLightContent)
        setNavigationBarColor(
            color,
            darkIcons,
            isNavigationBarContrastEnforced,
            transformColorForLightContent
        )
    }
}

private val BlackScrim = Color(0f, 0f, 0f, 0.3f) // 30% opaque black
private val BlackScrimmed: (Color) -> Color = { original ->
    BlackScrim.compositeOver(original)
}

@Composable
expect fun rememberSystemUiController(): SystemUiController