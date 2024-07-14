package com.funny.translation.helper

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import com.funny.translation.kmp.NavHostController

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController has not been initialized! ")
}

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope> {
    error("LocalNavAnimatedVisibilityScope has not been initialized! ")
}

// TODO 当重新升级回 1.7.0 时，移除下面的
// 由于把代码从 1.7.0-alpha01 迁移回来了 1.6.2，这个类没得了，自己写一个
@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This is a next-version api"
)
annotation class ExperimentalSharedTransitionApi

@OptIn(ExperimentalSharedTransitionApi::class)
interface SharedTransitionScope {

}

private val SharedTransitionScopeImpl = object: SharedTransitionScope {}

@ExperimentalSharedTransitionApi
@Composable
fun SharedTransitionLayout(modifier: Modifier = Modifier, content: @Composable SharedTransitionScope.() -> Unit) {
    Layout(
        content = { SharedTransitionScopeImpl.content() },
        modifier = modifier
    ) { measurables, constraints ->
        layout(constraints.maxWidth, constraints.maxHeight) {
            measurables.forEach { measurable ->
                val placeable = measurable.measure(constraints)
                placeable.place(0, 0)
            }
        }
    }

}

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
    error("LocalSharedTransitionScope has not been initialized!")
}