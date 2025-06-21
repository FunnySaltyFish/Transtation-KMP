package com.funny.translation.kmp

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.times

expect fun Modifier.kmpImeNestedScroll(): Modifier

fun Modifier.ifThen(condition: Boolean, then: Modifier.() -> Modifier): Modifier {
    return if (condition) this.then(then(this)) else this
}

@Stable
fun Modifier.platformOnly(platform: Platform, modifier: Modifier): Modifier {
    return if (platform == currentPlatform) modifier else this
}

@Stable
fun Modifier.desktopOnly(modifier: Modifier): Modifier {
    return platformOnly(Platform.Desktop, modifier)
}

// 为主体内容增加一个跟随 drawer 移动而增强的模糊，灵感来源于 QQ 用户 2140303919
@Stable
fun Modifier.blurWithDrawer(
    drawerState: DrawerState,
    drawerWidthPx: Float,
    maxBlurSize: Dp = 12.dp
) = Modifier.blur(
    2 * maxBlurSize - lerp(maxBlurSize, 0.dp,  drawerState.currentOffset / drawerWidthPx),
    BlurredEdgeTreatment.Unbounded
)