@file:Suppress("unused")

/**
 * 更丝滑的惯性回弹效果，此代码来自 QQ 群贡献者 QQ 2140303919，原始修改自 https://github.com/Cormor/ComposeOverscroll/blob/main/overscroll_core/src/main/java/com/cormor/overscroll/core/OverScroll.kt
 */

package com.funny.translation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt

typealias ScrollEasing = (current: Float, delta: Float) -> Float

object OverScroll {
    object Defaults {
        const val SpringStiff = 150f          // 弹性刚度
        const val SpringDamp  = 0.73f         // 阻尼系数
        const val VisibleThr  = 0.5f          // 小于该阈值视为动画结束
        const val ParabolaP   = 50f           // 手感关键参数
    }

    private fun Float.sameSign(other: Float) = sign == other.sign

    @Stable
    fun parabolaEasing(
        current: Float,
        delta: Float,
        p: Float = Defaults.ParabolaP,
        density: Float = 4f
    ): Float {
        val realP = p * density
        val ratio = (realP / sqrt(realP * abs(current + delta / 2).coerceAtLeast(Float.MIN_VALUE)))
            .coerceIn(Float.MIN_VALUE, 1f)
        return if (current.sameSign(delta)) current + delta * ratio else current + delta
    }

    val DefaultEasing: ScrollEasing
        @Composable
        get() {
            val d = LocalDensity.current.density
            return { cur, dt -> parabolaEasing(cur, dt, density = d) }
        }

    @Suppress("ModifierFactoryUnreferencedReceiver")
    @Composable
    fun Modifier.overScrollVertical(
        nestedScrollToParent: Boolean = true,
        scrollEasing: ScrollEasing? = null,
        springStiff: Float = Defaults.SpringStiff,
        springDamp: Float = Defaults.SpringDamp
    ) = overscrollInternal(
        vertical = true,
        nestedScrollToParent,
        scrollEasing ?: DefaultEasing,
        springStiff,
        springDamp
    )

    @Suppress("ModifierFactoryUnreferencedReceiver")
    @Composable
    fun Modifier.overScrollHorizontal(
        nestedScrollToParent: Boolean = true,
        scrollEasing: ScrollEasing? = null,
        springStiff: Float = Defaults.SpringStiff,
        springDamp: Float = Defaults.SpringDamp
    ) = overscrollInternal(
        vertical = false,
        nestedScrollToParent,
        scrollEasing ?: DefaultEasing,
        springStiff,
        springDamp
    )

    // 核心
    @Suppress("NAME_SHADOWING")
    private fun Modifier.overscrollInternal(
        vertical: Boolean,
        nestedScrollToParent: Boolean,
        scrollEasing: ScrollEasing,
        springStiff: Float,
        springDamp: Float
    ) = composed {

        val dispatcher = remember { NestedScrollDispatcher() }
        var offset by remember { mutableFloatStateOf(0f) }

        val connection = remember(vertical, nestedScrollToParent, scrollEasing, springStiff, springDamp) {
            object : NestedScrollConnection {

                lateinit var rebound: Animatable<Float, AnimationVector1D>

                // 拖拽 
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (source != NestedScrollSource.UserInput) return dispatcher.dispatchPreScroll(available, source)

                    if (::rebound.isInitialized && rebound.isRunning) dispatcher.coroutineScope.launch { rebound.stop() }

                    val real = if (nestedScrollToParent) available - dispatcher.dispatchPreScroll(available, source) else available
                    val d = if (vertical) real.y else real.x

                    if (abs(offset) <= Defaults.VisibleThr || offset.sameSign(d)) return available - real

                    val next = scrollEasing(offset, d)
                    return if (!offset.sameSign(next)) {
                        offset = 0f
                        if (vertical) Offset(available.x - real.x, available.y - real.y + d)
                        else Offset(available.x - real.x + d, available.y - real.y)
                    } else {
                        offset = next
                        if (vertical) Offset(available.x - real.x, available.y) else Offset(available.x, available.y - real.y)
                    }
                }

                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    if (source != NestedScrollSource.UserInput) return dispatcher.dispatchPreScroll(available, source)

                    val real = if (nestedScrollToParent) available - dispatcher.dispatchPostScroll(consumed, available, source) else available
                    offset = scrollEasing(offset, if (vertical) real.y else real.x)
                    return if (vertical) Offset(available.x - real.x, available.y) else Offset(available.x, available.y - real.y)
                }

                // 惯性 
                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (::rebound.isInitialized && rebound.isRunning) rebound.stop()

                    val parent = if (nestedScrollToParent) dispatcher.dispatchPreFling(available) else Velocity.Zero
                    val real = available - parent
                    var rest = if (vertical) real.y else real.x

                    if (abs(offset) >= Defaults.VisibleThr && !offset.sameSign(rest)) {
                        rebound = Animatable(offset).apply {
                            if (rest < 0) updateBounds(lowerBound = 0f) else if (rest > 0) updateBounds(upperBound = 0f)
                        }
                        val result = rebound.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(dampingRatio = springDamp, stiffness = springStiff),
                            initialVelocity = rest
                        ) { offset = scrollEasing(offset, value - offset) }
                        // Compose 1.6+: velocityVector；旧版本无此字段则为 0
                        rest = result.endState.runCatching { velocityVector.value }.getOrDefault(0f)
                    }
                    return if (vertical) Velocity(parent.x, available.y - rest) else Velocity(available.x - rest, parent.y)
                }

                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    val real = if (nestedScrollToParent) available - dispatcher.dispatchPostFling(consumed, available) else available
                    rebound = Animatable(offset)
                    rebound.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = springDamp, stiffness = springStiff),
                        initialVelocity = if (vertical) real.y else real.x
                    ) { offset = scrollEasing(offset, value - offset) }
                    return if (vertical) Velocity(available.x - real.x, available.y) else Velocity(available.x, available.y - real.y)
                }
            }
        }

        this.clipToBounds()
            .nestedScroll(connection, dispatcher)
            .graphicsLayer { if (vertical) translationY = offset else translationX = offset }
    }

    @Composable
    fun rememberOverscrollFlingBehavior(
        decaySpec: DecayAnimationSpec<Float> = exponentialDecay(),
        getScrollState: () -> ScrollableState
    ): FlingBehavior = remember(decaySpec, getScrollState) {
        object : FlingBehavior {
            private val Float.unconsumable
                get() = !(this < 0 && getScrollState().canScrollBackward || this > 0 && getScrollState().canScrollForward)

            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                if (initialVelocity.unconsumable) return initialVelocity
                if (abs(initialVelocity) <= 1f) return initialVelocity

                var rest = initialVelocity
                var last = 0f
                AnimationState(0f, initialVelocity).animateDecay(decaySpec) {
                    val delta = value - last
                    val consumed = scrollBy(delta)
                    last = value
                    rest = this.velocity
                    if (abs(delta - consumed) > 0.5f || rest.unconsumable) cancelAnimation()
                }
                return rest
            }
        }
    }
}
