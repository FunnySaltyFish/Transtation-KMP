package com.funny.translation.kmp

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset

// 由于原本的 slideInHorizontally 和 slideOutHorizontally 只在 AnimatedContentTransitionScope 里提供
// PreCompose 提供的函数并没有这玩意儿，所以这里没办法自己实现
fun slideIntoContainer(
    direction: AnimatedContentTransitionScope.SlideDirection,
    animationSpec: FiniteAnimationSpec<IntOffset> = tween()
): EnterTransition {
    return when (direction) {
        AnimatedContentTransitionScope.SlideDirection.Left, AnimatedContentTransitionScope.SlideDirection.Start -> {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = animationSpec,
            )
        }
        AnimatedContentTransitionScope.SlideDirection.Right, AnimatedContentTransitionScope.SlideDirection.End -> {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = animationSpec,
            )
        }
        AnimatedContentTransitionScope.SlideDirection.Up -> {
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = animationSpec,
            )
        }
        AnimatedContentTransitionScope.SlideDirection.Down -> {
            slideInVertically(
                initialOffsetY = { -it },
                animationSpec = animationSpec,
            )
        }

        else -> error("no such case")
    }
}

fun slideOutOfContainer(
    direction: AnimatedContentTransitionScope.SlideDirection,
    animationSpec: FiniteAnimationSpec<IntOffset> = tween()
): ExitTransition {
    return when (direction) {
        AnimatedContentTransitionScope.SlideDirection.Left, AnimatedContentTransitionScope.SlideDirection.Start -> {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = animationSpec,
            )
        }
        AnimatedContentTransitionScope.SlideDirection.Right, AnimatedContentTransitionScope.SlideDirection.End -> {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = animationSpec,
            )
        }
        AnimatedContentTransitionScope.SlideDirection.Up -> {
            slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = animationSpec,
            )
        }
        AnimatedContentTransitionScope.SlideDirection.Down -> {
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = animationSpec,
            )
        }

        else -> error("no such case")
    }
}