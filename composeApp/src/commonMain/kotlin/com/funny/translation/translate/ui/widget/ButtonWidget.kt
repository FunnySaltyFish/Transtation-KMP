package com.funny.translation.translate.ui.widget

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.kmp.painterDrawableRes
import com.funny.translation.strings.ResStrings
import com.funny.translation.ui.FixedSizeIcon
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Composable
fun RoundCornerButton(
    text : String,
    modifier: Modifier = Modifier,
    background : Color = MaterialTheme.colorScheme.secondary,
    onClick : ()->Unit = {},
    extraContent : @Composable ()->Unit
) {
    Button(onClick = onClick, shape = CircleShape, modifier=modifier, colors = buttonColors(containerColor = background), contentPadding = PaddingValues(horizontal = 36.dp,vertical = 12.dp)) {
        Text(text = text, color = Color.White)
        extraContent()
    }
}

@ExperimentalAnimationApi
@Composable
fun SelectableChip(
    initialSelect : Boolean = false,
    text: String = "",
    onClick: () -> Unit
) {
    var selected by remember {
        mutableStateOf(initialSelect)
    }
    val background by animateColorAsState(targetValue = if(selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface)
    val textColor by animateColorAsState(targetValue = if(selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface.copy(0.5f))

    //val border = if (selected) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, textColor)
    Button(
        onClick = {
            onClick()
            selected = !selected
        },
        shape = CircleShape,
        modifier = Modifier.selectable(selected, true, null, {}),
        colors = buttonColors(contentColor = textColor, containerColor = background),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = textColor, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ExchangeButton(
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick: () -> Unit = {},
) {
    var clickOnce by remember {
        mutableStateOf(false)
    }
    val rotateValue by animateFloatAsState(targetValue = if(clickOnce) 0f else 180f)
    IconButton(
        onClick = {
            clickOnce = !clickOnce
            onClick()
        },
        Modifier
            .background(
                Color.Transparent,
                CircleShape
            ).graphicsLayer {
                rotationZ = rotateValue
            }
    ) {
        FixedSizeIcon(
            painterDrawableRes("ic_exchange"), tint = tint,
            contentDescription = ResStrings.exchange,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 展开状态枚举
 */
enum class ExpandState {
    COLLAPSED,      // 完全收折
    PARTIAL,        // 部分展开
    FULL           // 完全展开
}

/**
 * 支持两次收折的展开按钮
 *
 * @param modifier 修饰符
 * @param expandState 当前展开状态
 * @param supportTwoLevel 是否支持两级展开，false时退化为普通的单级展开
 * @param tint 图标颜色
 * @param onExpandChange 状态变化回调
 */
@Composable
fun ExpandButton(
    modifier: Modifier = Modifier,
    expandState: ExpandState,
    supportTwoLevel: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onExpandChange: (ExpandState) -> Unit
) {
    // 计算旋转角度
    val rotationValue by animateFloatAsState(
        targetValue = when {
            !supportTwoLevel -> if (expandState != ExpandState.COLLAPSED) -180f else 0f
            else -> if (expandState != ExpandState.FULL) 0f else 180f
        },
        animationSpec = tween(700),
        label = "expand_rotation"
    )

    // 选择合适的图标
    val icon: ImageVector = when {
        !supportTwoLevel -> Icons.Default.ArrowDropDown
        expandState == ExpandState.COLLAPSED -> Icons.Default.KeyboardDoubleArrowDown
        else -> Icons.Default.ArrowDropDown
    }

    IconButton(
        onClick = {
            val nextState = getNextExpandState(expandState, supportTwoLevel)
            onExpandChange(nextState)
        },
        modifier = modifier
    ) {
        FixedSizeIcon(
            imageVector = icon,
            contentDescription = getContentDescription(expandState, supportTwoLevel),
            modifier = Modifier.graphicsLayer {
                rotationX = rotationValue
            },
            tint = tint
        )
    }
}

/**
 * 获取下一个展开状态
 */
private fun getNextExpandState(currentState: ExpandState, supportTwoLevel: Boolean): ExpandState {
    return if (!supportTwoLevel) {
        // 单级模式：只在 COLLAPSED 和 FULL 之间切换
        when (currentState) {
            ExpandState.COLLAPSED -> ExpandState.FULL
            else -> ExpandState.COLLAPSED
        }
    } else {
        // 双级模式：按循环顺序切换
        when (currentState) {
            ExpandState.COLLAPSED -> ExpandState.PARTIAL
            ExpandState.PARTIAL -> ExpandState.FULL
            ExpandState.FULL -> ExpandState.COLLAPSED
        }
    }
}

/**
 * 获取内容描述文本
 */
private fun getContentDescription(expandState: ExpandState, supportTwoLevel: Boolean): String {
    return if (!supportTwoLevel) {
        when (expandState) {
            ExpandState.COLLAPSED -> "展开"
            else -> "收折"
        }
    } else {
        when (expandState) {
            ExpandState.COLLAPSED -> "展开"
            ExpandState.PARTIAL -> "完全展开"
            ExpandState.FULL -> "收折"
        }
    }
}

/**
 * 兼容现有 ExpandMoreButton 的扩展函数
 */
@Composable
fun ExpandMoreButton(
    modifier: Modifier = Modifier,
    expand: Boolean,
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick: (Boolean) -> Unit
) {
    val expandState = if (expand) ExpandState.PARTIAL else ExpandState.COLLAPSED

    ExpandButton(
        modifier = modifier,
        expandState = expandState,
        supportTwoLevel = false,
        tint = tint,
        onExpandChange = { newState ->
            onClick(newState != ExpandState.COLLAPSED)
        }
    )
}
