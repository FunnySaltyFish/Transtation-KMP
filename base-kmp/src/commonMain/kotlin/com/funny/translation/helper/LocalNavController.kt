package com.funny.translation.translate

import androidx.compose.runtime.staticCompositionLocalOf
import com.funny.translation.kmp.NavHostController

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController has not been initialized! ")
}