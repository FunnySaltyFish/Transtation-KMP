package com.funny.translation.kmp

// DesktopModule
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
actual fun rememberSystemUiController(): SystemUiController {
    return EmptySystemUiController
}

private val EmptySystemUiController = object : SystemUiController {
    override var systemBarsBehavior: Int = 0
    override var isStatusBarVisible: Boolean = true
    override var isNavigationBarVisible: Boolean = true

    override fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean,
        transformColorForLightContent: (Color) -> Color
    ) { }

    override fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean,
        navigationBarContrastEnforced: Boolean,
        transformColorForLightContent: (Color) -> Color
    ) { }

    override var statusBarDarkContentEnabled: Boolean = false
    override var navigationBarDarkContentEnabled: Boolean = false
    override var systemBarsDarkContentEnabled: Boolean = false
    override var isNavigationBarContrastEnforced: Boolean = false
}