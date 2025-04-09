package com.github.only52607.compose.window

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

val LocalFloatingWindow: ProvidableCompositionLocal<ComposeFloatingWindow> = compositionLocalOf {
    noLocalProvidedFor("LocalFloatingWindow")
}

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}