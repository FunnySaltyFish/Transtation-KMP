package com.funny.translation.ui.dialog // Replace with your actual package name

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import com.funny.translation.helper.SimpleAction
import com.funny.translation.kmp.base.strings.ResStrings

@Composable
actual fun AnyPopDialog(
    state: AnyPopDialogState,
    modifier: Modifier,
    properties: AnyPopDialogProperties,
    onDismissRequest: SimpleAction,
    content: @Composable ColumnScope.() -> Unit
) {
    if (state.isVisible) {
        DialogWindow(
            onCloseRequest = onDismissRequest, // Primary way to close Desktop dialogs
            state = rememberDialogState(
                size = DpSize(width = 800.dp, height = 600.dp), // Default size
            ),
//            undecorated = true, // Common for custom popups
//            transparent = true, // Needed for custom background/dimming
//            resizable = false,  // Usually popups aren't resizable
            focusable = true,   // Allow keyboard interactions if needed within the dialog
            title = ResStrings.app_name,
            // Desktop Dialog state can manage position, not directly needed here
            // state = rememberDialogState(),
            // The `visible` parameter here is tricky. Let's rely on `if(renderDialog)`
            // visible = transitionVisible, // This might fight with our LaunchedEffect logic
            onKeyEvent = { // Manual Back Press handling for Desktop (Escape key)
                if (properties.dismissOnBackPress && it.key == Key.Escape && it.type == KeyEventType.KeyDown) {
                    onDismissRequest()
                    true // Consume event
                } else {
                    false // Don't consume other events
                }
            },
            // Desktop doesn't have DialogProperties like Android. Configure via parameters.
            content = {
                // --- Content similar to Android but without Android-specific windowing ---

                val animColor = remember { Animatable(Color.Transparent) }

                LaunchedEffect(Unit) {
                    val targetColor = if (state.isVisible) {
                        Color.Black.copy(alpha = 0.45F) // Use same dimming
                    } else {
                        Color.Transparent
                    }
                    animColor.animateTo(
                        targetValue = targetColor,
                        animationSpec = tween(properties.durationMillis)
                    )
                }

                // Desktop window doesn't have the same complex setup.
                // Size is determined by content unless explicitly set via windowState or modifiers.

                // On Desktop, Dialog fills the window provided by the Dialog composable.
                // To achieve full-screen dimming, we need the Box to fill *that* window.
                Box(
                    modifier = Modifier
                        .fillMaxSize() // Fill the Dialog's implicit window
                        .background(Color.Transparent), // Box itself is transparent
                    contentAlignment = when (properties.direction) {
                        DirectionState.TOP -> Alignment.TopCenter
                        DirectionState.LEFT -> Alignment.CenterStart
                        DirectionState.RIGHT -> Alignment.CenterEnd
                        DirectionState.BOTTOM -> Alignment.BottomCenter
                    }
                ) {
                    // Background Scrim - fills the Box
                    Spacer(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(animColor.value)
                            .clickOutSideModifier( // Use the common modifier
                                enabled = properties.dismissOnClickOutside,
                                onTap = onDismissRequest
                            )
                    )

                    Column(modifier = modifier) {
                        content()
                    }
                } // End Root Box

                // Note: Desktop's BackHandler equivalent is handled via onKeyEvent (Escape key)
            }) // End Dialog composable
    } // End if(renderDialog)
}

@Immutable
actual class AnyPopDialogProperties(
    actual val dismissOnBackPress: Boolean = true,
    actual val dismissOnClickOutside: Boolean = true,
    actual val direction: DirectionState = DirectionState.BOTTOM,
    actual val durationMillis: Int = DefaultDurationMillis,
    val backgroundDimEnabled: Boolean = true,
) {
    actual constructor(
        dismissOnBackPress: Boolean,
        dismissOnClickOutside: Boolean,
        direction: DirectionState,
        durationMillis: Int
    ) : this(
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        direction = direction,
        backgroundDimEnabled = true,
        durationMillis = durationMillis
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnyPopDialogProperties) return false

        if (dismissOnBackPress != other.dismissOnBackPress) return false
        if (dismissOnClickOutside != other.dismissOnClickOutside) return false
        if (direction != other.direction) return false
        if (backgroundDimEnabled != other.backgroundDimEnabled) return false
        if (durationMillis != other.durationMillis) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dismissOnBackPress.hashCode()
        result = 31 * result + dismissOnClickOutside.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + backgroundDimEnabled.hashCode()
        result = 31 * result + durationMillis.hashCode()
        return result
    }
}