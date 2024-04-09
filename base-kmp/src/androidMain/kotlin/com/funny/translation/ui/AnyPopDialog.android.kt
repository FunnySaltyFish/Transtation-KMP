package com.funny.translation.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.core.view.WindowCompat
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

@Composable
internal actual fun DialogFullScreen(
    isActiveClose: Boolean,
    onDismissRequest: () -> Unit,
    properties: AnyPopDialogProperties,
    content: @Composable () -> Unit
) {
    var isAnimateLayout by remember {
        mutableStateOf(false)
    }
    var isBackPress by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(isActiveClose) {
        if (isActiveClose) {
            isBackPress = true
            isAnimateLayout = false
        }
    }

    val handleBackPress = {
        if (!isBackPress) {
            isBackPress = true
            isAnimateLayout = false
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            // 这里不使用新的测量规范，不能设置为false
            usePlatformDefaultWidth = true,
            decorFitsSystemWindows = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            securePolicy = properties.securePolicy
        ),
        content = {
            val animColor = remember { Animatable(Color.Transparent) }
            LaunchedEffect(isAnimateLayout) {
                if(properties.backgroundDimEnabled) {
                    animColor.animateTo(
                        if (isAnimateLayout) Color.Black.copy(alpha = 0.45F) else Color.Transparent,
                        animationSpec = tween(properties.durationMillis)
                    )
                } else {
                    delay(properties.durationMillis.toLong())
                }
                if (!isAnimateLayout) {
                    onDismissRequest.invoke()
                }
            }
            val activityWindow = getActivityWindow()
            val dialogWindow = getDialogWindow()
            val parentView = LocalView.current.parent as View
            SideEffect {
                if (activityWindow != null && dialogWindow != null && !isBackPress && !isAnimateLayout) {
                    val attributes = WindowManager.LayoutParams()
                    attributes.copyFrom(activityWindow.attributes)
                    attributes.type = dialogWindow.attributes.type
                    dialogWindow.attributes = attributes
                    // 修复Android10 - Android11出现背景全黑的情况
                    dialogWindow.setBackgroundDrawableResource(android.R.color.transparent)

                    dialogWindow.setLayout(
                        activityWindow.decorView.width,
                        activityWindow.decorView.height
                    )
                    // 修复Android低版本系统，状态栏和导航栏颜色问题
                    dialogWindow.statusBarColor = properties.statusBarColor.toArgb()
                    dialogWindow.navigationBarColor = properties.navBarColor.toArgb()

                    WindowCompat.getInsetsController(dialogWindow, parentView)
                        .isAppearanceLightNavigationBars = properties.isAppearanceLightNavigationBars
                    isAnimateLayout = true
                }
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = when(properties.direction){
                    DirectionState.TOP -> Alignment.TopCenter
                    DirectionState.LEFT -> Alignment.CenterStart
                    DirectionState.RIGHT -> Alignment.CenterEnd
                    else -> Alignment.BottomCenter
                }
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(animColor.value)
                        .clickOutSideModifier(
                            dismissOnClickOutside = properties.dismissOnClickOutside,
                            onTap = handleBackPress
                        )
                )
                AnimatedVisibility(
                    modifier = Modifier.pointerInput(Unit) {},
                    visible = isAnimateLayout,
                    enter = when (properties.direction) {
                        DirectionState.TOP -> slideInVertically(initialOffsetY = { -it })
                        DirectionState.LEFT -> slideInHorizontally(initialOffsetX = { -it })
                        DirectionState.RIGHT -> slideInHorizontally(initialOffsetX = { it })
                        else -> slideInVertically(initialOffsetY = { it })
                    },
                    exit = when (properties.direction) {
                        DirectionState.TOP -> fadeOut() + slideOutVertically(targetOffsetY = { -it })
                        DirectionState.LEFT -> fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
                        DirectionState.RIGHT -> fadeOut() + slideOutHorizontally(targetOffsetX = { it })
                        else -> fadeOut() + slideOutVertically(targetOffsetY = { it })
                    }
                ) {
                    content()
                }
            }
            BackHandler(enabled = properties.dismissOnBackPress, onBack = handleBackPress)
        }
    )
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
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val isAppearanceLightNavigationBars: Boolean = true,
    val direction: DirectionState,
    val backgroundDimEnabled: Boolean = true,
    val statusBarColor: Color = Color.Transparent,
    val navBarColor: Color = Color.Transparent,
    val durationMillis: Int = DefaultDurationMillis,
    val securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit
) {
    actual constructor(direction: DirectionState): this(direction = direction, securePolicy = SecureFlagPolicy.Inherit)

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