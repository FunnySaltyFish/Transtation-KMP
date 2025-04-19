// File: AnyPopDialog.kt
package com.funny.translation.ui.dialog

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.funny.translation.helper.SimpleAction
import com.funny.translation.kmp.base.strings.ResStrings

// Adapted from https://github.com/TheMelody/AnyPopDialog-Compose/blob/main/any_pop_dialog_library/src/main/java/com/melody/dialog/any_pop/AnyPopDialog.kt

internal const val DefaultDurationMillis: Int = 250

@Composable
fun AnyPopDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    state: AnyPopDialogState = rememberAnyPopDialogState(),
    properties: AnyPopDialogProperties = AnyPopDialogProperties(direction = DirectionState.BOTTOM),
    onConfirm: SimpleAction?,
    confirmButton: @Composable () -> Unit = {
        if (onConfirm != null) {
            TextButton(onClick = onConfirm) {
                Text(ResStrings.confirm)
            }
        }
    },
    onDismiss: SimpleAction? = onDismissRequest,
    dismissButton: @Composable () -> Unit = {
        if (onDismiss != null) {
            TextButton(onClick = onDismiss) {
                Text(ResStrings.cancel)
            }
        }
    },
    text: @Composable () -> Unit
) {
    AnyPopDialog(
        modifier = modifier,
        state = state,
        properties = properties,
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                text()
                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End),
                ) {
                    dismissButton()
                    if (confirmButton != {}) {
                        Spacer(Modifier.width(4.dp))
                        confirmButton()
                    }
                }
            }
        }
    )
}


@Composable
expect fun AnyPopDialog(
    state: AnyPopDialogState,
    modifier: Modifier,
    properties: AnyPopDialogProperties = AnyPopDialogProperties(),
    onDismissRequest: SimpleAction = { state.animateHide() },
    content: @Composable ColumnScope.() -> Unit
)


// Helper functions for transitions (common)
internal fun enterTransition(direction: DirectionState, duration: Int): EnterTransition {
    val spec = tween<IntOffset>(duration, easing = FastOutSlowInEasing)
    return when (direction) {
        DirectionState.TOP -> slideInVertically(spec) { -it }
        DirectionState.BOTTOM -> slideInVertically(spec) { it }
        DirectionState.LEFT -> slideInHorizontally(spec) { -it }
        DirectionState.RIGHT -> slideInHorizontally(spec) { it }
    } + fadeIn(animationSpec = tween(duration))
}

internal fun exitTransition(direction: DirectionState, duration: Int): ExitTransition {
    val spec = tween<IntOffset>(duration, easing = FastOutSlowInEasing) // Or LinearOutSlowIn
    return when (direction) {
        DirectionState.TOP -> slideOutVertically(spec) { -it }
        DirectionState.BOTTOM -> slideOutVertically(spec) { it }
        DirectionState.LEFT -> slideOutHorizontally(spec) { -it }
        DirectionState.RIGHT -> slideOutHorizontally(spec) { it }
    } + fadeOut(animationSpec = tween(duration))
}

/**
 * @param dismissOnBackPress Whether pressing back dismisses the dialog
 * @param dismissOnClickOutside Whether clicking outside dismisses the dialog 
 * @param isAppearanceLightNavigationBars Whether navigation bar icons use light appearance
 * @param direction The direction from which the dialog appears
 * @param backgroundDimEnabled Whether to dim the background behind the dialog
 * @param durationMillis Animation duration for appearance/disappearance
 * @param securePolicy Dialog security policy
 */
@Immutable
expect class AnyPopDialogProperties(
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    direction: DirectionState = DirectionState.BOTTOM,
    durationMillis: Int = DefaultDurationMillis,
) {
    val dismissOnBackPress: Boolean
    val dismissOnClickOutside: Boolean
    val direction: DirectionState
    val durationMillis: Int
}

enum class DirectionState {
    TOP,
    LEFT,
    RIGHT,
    BOTTOM
}

// Actual implementation - likely common
internal fun Modifier.clickOutSideModifier(
    enabled: Boolean,
    onTap: () -> Unit
): Modifier = this.then(
    if (enabled) {
        Modifier.pointerInput(Unit) { // Using Unit ensures it runs once
            detectTapGestures(onTap = {
                onTap()
            })
        }
    } else Modifier
)