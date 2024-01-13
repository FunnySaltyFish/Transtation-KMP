package com.funny.translation.kmp

import androidx.compose.ui.Modifier

expect fun Modifier.kmpImeNestedScroll(): Modifier

fun Modifier.ifThen(condition: Boolean, then: Modifier): Modifier {
    return if (condition) then else this
}