package com.funny.translation.translate.ui.main.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
internal fun ChildrenFixedSizeRow(
    modifier: Modifier = Modifier,
    elementsPadding: Dp = 40.dp,
    left: @Composable () -> Unit,
    center: @Composable () -> Unit,
    right: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val ep = remember(elementsPadding) {
        density.run {
            elementsPadding.toPx().roundToInt()
        }
    }
    SubcomposeLayout(modifier) { constraints: Constraints ->
        val allWidth = constraints.maxWidth
        val centerPlaceable = subcompose("center", center).first().measure(
            constraints.copy(minWidth = 0)
        )
        val centerWidth = centerPlaceable.width
        val w = ((allWidth - centerWidth - 2 * ep) / 2).coerceAtLeast(0)
        val leftPlaceable = subcompose("left", left).first().measure(
            constraints.copy(minWidth = w, maxWidth = w)
        )
        val rightPlaceable = subcompose("right", right).first().measure(
            constraints.copy(minWidth = w, maxWidth = w)
        )
        val h = maxOf(centerPlaceable.height, leftPlaceable.height, rightPlaceable.height)
        layout(constraints.maxWidth, h) {
            leftPlaceable.placeRelative(0, (h - leftPlaceable.height) / 2)
            centerPlaceable.placeRelative(w + ep, (h - centerPlaceable.height) / 2)
            rightPlaceable.placeRelative(allWidth - w, (h - rightPlaceable.height) / 2)
        }
    }
}