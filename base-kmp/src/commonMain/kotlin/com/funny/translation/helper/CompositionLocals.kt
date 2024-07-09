package com.funny.translation.helper

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.funny.translation.kmp.NavHostController

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController has not been initialized! ")
}

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope> {
    error("LocalNavAnimatedVisibilityScope has not been initialized! ")
}

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
    error("LocalSharedTransitionScope has not been initialized!")
}