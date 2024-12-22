package com.funny.translation.kmp

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

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