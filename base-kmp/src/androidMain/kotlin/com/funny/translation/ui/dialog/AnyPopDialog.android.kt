// File: AnyPopDialogAndroid.kt
package com.funny.translation.ui.dialog

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.core.view.WindowCompat
import com.funny.translation.helper.Log
import com.funny.translation.helper.SimpleAction
import com.funny.translation.kmp.BackHandler
import kotlinx.coroutines.delay

@Composable
private fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window

@Composable
private fun getActivityWindow(): Window? = LocalView.current.context.getActivityWindow()

private tailrec fun Context.getActivityWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.getActivityWindow()
    else -> null
}

private fun Context.getDisplayWidth(): Int {
    val displayMetrics = resources.displayMetrics
    return displayMetrics.widthPixels
}

private fun Context.getDisplayHeight(): Int {
    val displayMetrics = resources.displayMetrics
    return displayMetrics.heightPixels
}

private const val TAG = "AnyPopDialog"

@Composable
actual fun AnyPopDialog(
    state: AnyPopDialogState,
    modifier: Modifier,
    properties: AnyPopDialogProperties,
    onDismissRequest: SimpleAction,
    content: @Composable ColumnScope.() -> Unit
) {
    val isVisible = state.isVisible
    var transitionVisible by remember { mutableStateOf(isVisible) }
    var renderDialog by remember { mutableStateOf(isVisible) }
    // Key to force Dialog recomposition on show

    LaunchedEffect(state.isVisible) {
        Log.d(TAG, "isVisible: ${state.isVisible}, renderDialog: $renderDialog")
        if (state.isVisible) {
            // Increment key *before* setting renderDialog true
            // Ensures the key is fresh when Dialog enters composition
            renderDialog = true
//            // Short delay might be needed to allow composition before animation starts? Optional.
//            // delay(1)
//            transitionVisible = true
        } else {
            transitionVisible = false
            delay(properties.durationMillis.toLong())
            renderDialog = false
        }
    }

    // This effect handles the background color animation/snapping
    val animColor = remember { Animatable(Color.Transparent) }
    LaunchedEffect(transitionVisible, properties.backgroundDimEnabled) {
        val targetColor = if (transitionVisible && properties.backgroundDimEnabled) {
            Color.Black.copy(alpha = 0.45F)
        } else {
            Color.Transparent
        }

        animColor.animateTo(
            targetValue = targetColor,
            animationSpec = tween(properties.durationMillis)
        )
    }


    if (renderDialog) {
        // Apply the key here to the Dialog composable
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                usePlatformDefaultWidth = true,
                decorFitsSystemWindows = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                securePolicy = properties.securePolicy
            ),
            content = {
                // --- Content based on DialogFullScreen ---
                val isPreview = LocalInspectionMode.current

                // Background color animation is now handled by the separate LaunchedEffect above

                val activityWindow = getActivityWindow()
                val dialogWindow = getDialogWindow()
                val parentView = LocalView.current.parent as View
                val context = LocalContext.current

                val displayWidth = remember(activityWindow?.decorView?.width) {
                    activityWindow?.decorView?.width ?: context.getDisplayWidth()
                }
                val displayHeight = remember(activityWindow?.decorView?.height) {
                    activityWindow?.decorView?.height ?: context.getDisplayHeight()
                }

                SideEffect {
                    if (dialogWindow != null && !isPreview) {
                        val attributes = WindowManager.LayoutParams()
                        if (activityWindow != null) {
                            attributes.copyFrom(activityWindow.attributes)
                        }
                        attributes.type = dialogWindow.attributes.type
                        dialogWindow.attributes = attributes

                        // 修复Android10 - Android11出现背景全黑的情况
                        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent)
                        dialogWindow.setLayout(displayWidth, displayHeight)
                        // 修复Android低版本系统，状态栏和导航栏颜色问题
                        dialogWindow.statusBarColor = properties.statusBarColor.toArgb()
                        dialogWindow.navigationBarColor = properties.navBarColor.toArgb()

                        WindowCompat.getInsetsController(dialogWindow, parentView)
                            .isAppearanceLightNavigationBars = properties.isAppearanceLightNavigationBars

                        if (state.isVisible) {
                            transitionVisible = true
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = when (properties.direction) {
                        DirectionState.TOP -> Alignment.TopCenter
                        DirectionState.LEFT -> Alignment.CenterStart
                        DirectionState.RIGHT -> Alignment.CenterEnd
                        DirectionState.BOTTOM -> Alignment.BottomCenter
                    }
                ) {
                    // Background Scrim - Use the animColor state directly
                    Spacer(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(animColor.value) // Use the state value
                            .clickOutSideModifier(
                                enabled = properties.dismissOnClickOutside,
                                onTap = onDismissRequest
                            )
                    )

                    // Animated Content
                    AnimatedVisibility(
                        modifier = Modifier.pointerInput(Unit) {},
                        visible = transitionVisible, // Still controls content animation
                        enter = enterTransition(properties.direction, properties.durationMillis),
                        exit = exitTransition(properties.direction, properties.durationMillis)
                    ) {
                        Column(modifier = modifier.navigationBarsPadding()) {
                            content()
                        }
                    }
                }

                BackHandler(enabled = properties.dismissOnBackPress && transitionVisible, onBack = onDismissRequest)
            }
        ) // End Dialog
} // End if(renderDialog)
}

/**
 * @param dismissOnBackPress 是否支持返回关闭Dialog
 * @param dismissOnClickOutside 是否支持空白区域点击关闭Dialog
 * @param isAppearanceLightNavigationBars 导航栏前景色是不是亮色
 * @param direction 当前对话框弹出的方向
 * @param backgroundDimEnabled 背景渐入检出开关
 * @param durationMillis 弹框消失和进入的时长
 * @param statusBarColor 外部传入设置状态栏颜色，默认透明没有颜色，**建议**:传你自己的Activity状态栏颜色
 * @param navBarColor 外部传入导航栏颜色，默认透明没有颜色，**建议**:传你自己的Activity导航栏颜色
 * @param securePolicy 屏幕安全策略
 */
@Immutable
actual class AnyPopDialogProperties(
    actual val dismissOnBackPress: Boolean = true,
    actual val dismissOnClickOutside: Boolean = true,
    actual val durationMillis: Int = DefaultDurationMillis,
    actual val direction: DirectionState = DirectionState.BOTTOM,
    val isAppearanceLightNavigationBars: Boolean = true,
    val backgroundDimEnabled: Boolean = true,
    val statusBarColor: Color = Color.Transparent,
    val navBarColor: Color = Color.Transparent,
    val securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit
) {
    actual constructor(
        dismissOnBackPress: Boolean,
        dismissOnClickOutside: Boolean,
        direction: DirectionState,
        durationMillis: Int
    ) : this(
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        durationMillis = durationMillis,
        direction = direction,
        isAppearanceLightNavigationBars = true,
        backgroundDimEnabled = true,
        statusBarColor = Color.Transparent,
        navBarColor = Color.Transparent,
        securePolicy = SecureFlagPolicy.Inherit
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnyPopDialogProperties) return false

        if (dismissOnBackPress != other.dismissOnBackPress) return false
        if (isAppearanceLightNavigationBars != other.isAppearanceLightNavigationBars) return false
        if (dismissOnClickOutside != other.dismissOnClickOutside) return false
        if (direction != other.direction) return false
        if (backgroundDimEnabled != other.backgroundDimEnabled) return false
        if (durationMillis != other.durationMillis) return false
        if (securePolicy != other.securePolicy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dismissOnBackPress.hashCode()
        result = 31 * result + dismissOnClickOutside.hashCode()
        result = 31 * result + isAppearanceLightNavigationBars.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + backgroundDimEnabled.hashCode()
        result = 31 * result + durationMillis.hashCode()
        result = 31 * result + securePolicy.hashCode()
        return result
    }
}