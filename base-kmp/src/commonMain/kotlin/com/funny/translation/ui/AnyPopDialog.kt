package com.funny.translation.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.funny.translation.helper.SimpleAction
import com.funny.translation.kmp.base.strings.ResStrings

// Adapted from https://github.com/TheMelody/AnyPopDialog-Compose/blob/main/any_pop_dialog_library/src/main/java/com/melody/dialog/any_pop/AnyPopDialog.kt

internal const val DefaultDurationMillis: Int = 250

@Composable
internal expect fun DialogFullScreen(
    isActiveClose: Boolean,
    onDismissRequest: () -> Unit,
    properties: AnyPopDialogProperties,
    content: @Composable () -> Unit
)

/**
 * @author 被风吹过的夏天
 * @see <a href="https://github.com/TheMelody/AnyPopDialog-Compose">https://github.com/TheMelody/AnyPopDialog-Compose</a>
 * @param isActiveClose 设置为true可触发动画关闭Dialog，动画完自动触发[onDismissRequest]
 * @param properties Dialog相关配置
 * @param onDismissRequest Dialog关闭的回调
 * @param content 可组合项视图
 */
@Composable
fun AnyPopDialog(
    modifier: Modifier = Modifier,
    isActiveClose: Boolean = false,
    properties: AnyPopDialogProperties = AnyPopDialogProperties(direction = DirectionState.BOTTOM),
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    DialogFullScreen(
        isActiveClose = isActiveClose,
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(modifier = modifier.navigationBarsPadding()) {
            content()
        }
    }
}

@Composable
fun AnyPopDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    isActiveClose: Boolean = false,
    properties: AnyPopDialogProperties = AnyPopDialogProperties(direction = DirectionState.BOTTOM),
    onConfirm: SimpleAction? = onDismissRequest,
    confirmButton: @Composable () -> Unit = {
        if (onConfirm != null) {
            TextButton(onClick = onConfirm) {
                Text(ResStrings.confirm)
            }
        }
    },
    onDismiss: SimpleAction? = onDismissRequest,
    dismissButton: @Composable () -> Unit = {
        if (onDismiss != null) {
            TextButton(onClick = onDismiss) {
                Text(ResStrings.cancel)
            }
        }
    },
    text: @Composable () -> Unit
) {
    AnyPopDialog(
        modifier = modifier,
        isActiveClose = isActiveClose,
        properties = properties,
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                text()
                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End),
                ) {
                    dismissButton()
                    if (confirmButton != {}) {
                        Spacer(Modifier.width(4.dp))
                        confirmButton()
                    }
                }
            }
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
 * @param securePolicy 屏幕安全策略
 */
@Immutable
expect class AnyPopDialogProperties(direction: DirectionState)

enum class DirectionState {
    TOP,
    LEFT,
    RIGHT,
    BOTTOM
}

internal fun Modifier.clickOutSideModifier(
    dismissOnClickOutside: Boolean,
    onTap:()->Unit
) = this.then(
    if (dismissOnClickOutside) {
        Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                onTap()
            })
        }
    } else Modifier
)