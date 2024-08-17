package com.funny.translation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.funny.translation.helper.rememberSaveableStateOf
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Stable
fun Modifier.floatingActionBarModifier(
    initialOffset: Offset = Offset(-64f, -100f)
) = composed {
    var offset by rememberSaveableStateOf(
        value = initialOffset,
        saver = OffsetSaver,
    )
    this
        .fillMaxSize()
        .wrapContentSize(Alignment.BottomEnd)
        .offset { offset.asIntOffset() }
        .pointerInput(Unit) {
            detectDragGesturesAfterLongPress { change, dragAmount ->
                offset += dragAmount
            }
        }
}

@Stable
fun Modifier.popDialogShape() = composed {
    this.fillMaxWidth()
        .background(
            MaterialTheme.colorScheme.surface,
            RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        )
        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
}

private val OffsetSaver = listSaver<Offset, Float>(
    save = { listOf(it.x, it.y) },
    restore = { Offset(it[0], it[1]) }
)

@Stable
fun Modifier.slideIn() = this.then(SlideInElement())
//composed {
//    val animatable = Animatable(1f)
//    val scope = rememberCoroutineScope()
//    DisposableEffect(Unit) {
//        scope.launch {
//            animatable.animateTo(0f)
//        }
//        onDispose {
//            scope.launch {
//                animatable.snapTo(1f)
//            }
//        }
//    }
//    this.graphicsLayer {
//        val width = size.width
//        translationX = (width * animatable.value)
//    }
//}

private class SlideInNode: LayoutModifierNode, Modifier.Node() {
    private val animatable = Animatable(1f)

    override fun onAttach() {
        super.onAttach()
        coroutineScope.launch {
            animatable.animateTo(0f)
        }
    }

    override fun onDetach() {
        super.onDetach()
        coroutineScope.launch {
            animatable.snapTo(1f)
        }
    }

    override fun onReset() {
        super.onReset()
        coroutineScope.launch {
            animatable.snapTo(1f)
        }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        val width = placeable.width
        return layout(width, placeable.height) {
            placeable.place((width * animatable.value).roundToInt(), 0)
        }
    }
}

private class SlideInElement: ModifierNodeElement<SlideInNode>() {
    override fun create(): SlideInNode {
        return SlideInNode()
    }

    override fun update(node: SlideInNode) {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        return other is SlideInElement
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun toString(): String {
        return "SlideInElement"
    }


}