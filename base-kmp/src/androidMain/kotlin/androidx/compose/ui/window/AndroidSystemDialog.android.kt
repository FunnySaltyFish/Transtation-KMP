/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package androidx.compose.ui.window

//import androidx.compose.ui.util.fastForEach
//import androidx.compose.ui.util.fastMap
//import androidx.compose.ui.util.fastMaxBy
import android.content.Context
import android.graphics.Outline
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentDialog
import androidx.activity.addCallback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.R
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.ViewRootForInspector
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.collections.forEach as fastForEach
import kotlin.collections.map as fastMap
import kotlin.collections.maxBy as fastMaxBy

/**
 * Properties used to customize the behavior of a [SystemDialog].
 *
 * @property dismissOnBackPress whether the dialog can be dismissed by pressing the back button.
 * If true, pressing the back button will call onDismissRequest.
 * @property dismissOnClickOutside whether the dialog can be dismissed by clicking outside the
 * dialog's bounds. If true, clicking outside the dialog will call onDismissRequest.
 * @property securePolicy Policy for setting [WindowManager.LayoutParams.FLAG_SECURE] on the
 * dialog's window.
 * @property usePlatformDefaultWidth Whether the width of the dialog's content should be limited to
 * the platform default, which is smaller than the screen width.
 * @property decorFitsSystemWindows Sets [WindowCompat.setDecorFitsSystemWindows] value. Set to
 * `false` to use WindowInsets. If `false`, the
 * [soft input mode][WindowManager.LayoutParams.softInputMode] will be changed to
 * [WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE] and `android:windowIsFloating` is
 * set to `false` for Android [R][Build.VERSION_CODES.R] and earlier.
 */
@Immutable
class SystemDialogProperties constructor(
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
    val usePlatformDefaultWidth: Boolean = true,
    val decorFitsSystemWindows: Boolean = true
) {

    constructor(
        dismissOnBackPress: Boolean = true,
        dismissOnClickOutside: Boolean = true,
        securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
    ) : this(
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        securePolicy = securePolicy,
        usePlatformDefaultWidth = true,
        decorFitsSystemWindows = true
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SystemDialogProperties) return false

        if (dismissOnBackPress != other.dismissOnBackPress) return false
        if (dismissOnClickOutside != other.dismissOnClickOutside) return false
        if (securePolicy != other.securePolicy) return false
        if (usePlatformDefaultWidth != other.usePlatformDefaultWidth) return false
        if (decorFitsSystemWindows != other.decorFitsSystemWindows) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dismissOnBackPress.hashCode()
        result = 31 * result + dismissOnClickOutside.hashCode()
        result = 31 * result + securePolicy.hashCode()
        result = 31 * result + usePlatformDefaultWidth.hashCode()
        result = 31 * result + decorFitsSystemWindows.hashCode()
        return result
    }
}

/**
 * Opens a dialog with the given content.
 *
 * A dialog is a small window that prompts the user to make a decision or enter
 * additional information. A dialog does not fill the screen and is normally used
 * for modal events that require users to take an action before they can proceed.
 *
 * The dialog is visible as long as it is part of the composition hierarchy.
 * In order to let the user dismiss the Dialog, the implementation of [onDismissRequest] should
 * contain a way to remove the dialog from the composition hierarchy.
 *
 * Example usage:
 *
 * @sample androidx.compose.ui.samples.DialogSample
 *
 * @param onDismissRequest Executes when the user tries to dismiss the dialog.
 * @param properties [SystemDialogProperties] for further customization of this dialog's behavior.
 * @param content The content to be displayed inside the dialog.
 */
@Composable
fun SystemDialog(
    onDismissRequest: () -> Unit,
    properties: SystemDialogProperties = SystemDialogProperties(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val composition = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)
    val dialogId = rememberSaveable { UUID.randomUUID() }
    val dialog = remember(view, density) {
        SystemDialogWrapper(
            onDismissRequest,
            properties,
            view,
            layoutDirection,
            density,
            dialogId
        ).apply {
            setContent(composition) {
                // TODO(b/159900354): draw a scrim and add margins around the Compose Dialog, and
                //  consume clicks so they can't pass through to the underlying UI
                SystemDialogLayout(
                    Modifier.semantics { dialog() },
                ) {
                    currentContent()
                }
            }
        }
    }

    DisposableEffect(dialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        } else {
            dialog.window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        }
        dialog.show()

        onDispose {
            dialog.dismiss()
            dialog.disposeComposition()
        }
    }

    SideEffect {
        dialog.updateParameters(
            onDismissRequest = onDismissRequest,
            properties = properties,
            layoutDirection = layoutDirection
        )
    }
}

/**
 * Provides the underlying window of a dialog.
 *
 * Implemented by dialog's root layout.
 */
interface SystemDialogWindowProvider {
    val window: Window
}

@Suppress("ViewConstructor")
private class SystemDialogLayout(
    context: Context,
    override val window: Window
) : AbstractComposeView(context), SystemDialogWindowProvider {

    private var content: @Composable () -> Unit by mutableStateOf({})

    var usePlatformDefaultWidth = false

    override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
        private set

    fun setContent(parent: CompositionContext, content: @Composable () -> Unit) {
        setParentCompositionContext(parent)
        this.content = content
        shouldCreateCompositionOnAttachedToWindow = true
        createComposition()
    }

    @Override
    @JvmName("internalOnMeasure")
    fun _internalOnMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (usePlatformDefaultWidth) {
            super.internalOnMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            // usePlatformDefaultWidth false, so don't want to limit the dialog width to the Android
            // platform default. Therefore, we create a new measure spec for width, which
            // corresponds to the full screen width. We do the same for height, even if
            // ViewRootImpl gives it to us from the first measure.
            val displayWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(displayWidth, MeasureSpec.AT_MOST)
            val displayHeightMeasureSpec =
                MeasureSpec.makeMeasureSpec(displayHeight, MeasureSpec.AT_MOST)
            super.internalOnMeasure(displayWidthMeasureSpec, displayHeightMeasureSpec)
        }
    }

    @Override
    @JvmName("internalOnMeasure")
    fun _internalOnLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.internalOnLayout(changed, left, top, right, bottom)
        // Now set the content size as fixed layout params, such that ViewRootImpl knows
        // the exact window size.
        val child = getChildAt(0) ?: return
        window.setLayout(child.measuredWidth, child.measuredHeight)
    }

    private val displayWidth: Int
        get() {
            val density = context.resources.displayMetrics.density
            return (context.resources.configuration.screenWidthDp * density).roundToInt()
        }

    private val displayHeight: Int
        get() {
            val density = context.resources.displayMetrics.density
            return (context.resources.configuration.screenHeightDp * density).roundToInt()
        }

    @Composable
    override fun Content() {
        content()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private class SystemDialogWrapper(
    private var onDismissRequest: () -> Unit,
    private var properties: SystemDialogProperties,
    private val composeView: View,
    layoutDirection: LayoutDirection,
    density: Density,
    dialogId: UUID
) : ComponentDialog(
    /**
     * [Window.setClipToOutline] is only available from 22+, but the style attribute exists on 21.
     * So use a wrapped context that sets this attribute for compatibility back to 21.
     */
    ContextThemeWrapper(
        composeView.context,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || properties.decorFitsSystemWindows) {
            R.style.DialogWindowTheme
        } else {
            R.style.FloatingDialogWindowTheme
        }
    )
),
    ViewRootForInspector {

    private val dialogLayout: SystemDialogLayout

    // On systems older than Android S, there is a bug in the surface insets matrix math used by
    // elevation, so high values of maxSupportedElevation break accessibility services: b/232788477.
    private val maxSupportedElevation = 8.dp

    override val subCompositionView: AbstractComposeView get() = dialogLayout

    private val defaultSoftInputMode: Int

    init {
        val window = window ?: error("Dialog has no window")
        defaultSoftInputMode =
            window.attributes.softInputMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        @OptIn(ExperimentalComposeUiApi::class)
        WindowCompat.setDecorFitsSystemWindows(window, properties.decorFitsSystemWindows)
        dialogLayout = SystemDialogLayout(context, window).apply {
            // Set unique id for AbstractComposeView. This allows state restoration for the state
            // defined inside the Dialog via rememberSaveable()
            setTag(R.id.compose_view_saveable_id_tag, "Dialog:$dialogId")
            // Enable children to draw their shadow by not clipping them
            clipChildren = false
            // Allocate space for elevation
            with(density) { elevation = maxSupportedElevation.toPx() }
            // Simple outline to force window manager to allocate space for shadow.
            // Note that the outline affects clickable area for the dismiss listener. In case of
            // shapes like circle the area for dismiss might be to small (rectangular outline
            // consuming clicks outside of the circle).
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, result: Outline) {
                    result.setRect(0, 0, view.width, view.height)
                    // We set alpha to 0 to hide the view's shadow and let the composable to draw
                    // its own shadow. This still enables us to get the extra space needed in the
                    // surface.
                    result.alpha = 0f
                }
            }
        }

        /**
         * Disables clipping for [this] and all its descendant [ViewGroup]s until we reach a
         * [SystemDialogLayout] (the [ViewGroup] containing the Compose hierarchy).
         */
        fun ViewGroup.disableClipping() {
            clipChildren = false
            if (this is SystemDialogLayout) return
            for (i in 0 until childCount) {
                (getChildAt(i) as? ViewGroup)?.disableClipping()
            }
        }

        // Turn of all clipping so shadows can be drawn outside the window
        (window.decorView as? ViewGroup)?.disableClipping()
        setContentView(dialogLayout)
        dialogLayout.setViewTreeLifecycleOwner(composeView.findViewTreeLifecycleOwner())
        dialogLayout.setViewTreeViewModelStoreOwner(composeView.findViewTreeViewModelStoreOwner())
        dialogLayout.setViewTreeSavedStateRegistryOwner(
            composeView.findViewTreeSavedStateRegistryOwner()
        )

        // Initial setup
        updateParameters(onDismissRequest, properties, layoutDirection)

        // Due to how the onDismissRequest callback works
        // (it enforces a just-in-time decision on whether to update the state to hide the dialog)
        // we need to unconditionally add a callback here that is always enabled,
        // meaning we'll never get a system UI controlled predictive back animation
        // for these dialogs
        onBackPressedDispatcher.addCallback(this) {
            if (properties.dismissOnBackPress) {
                onDismissRequest()
            }
        }
    }

    private fun setLayoutDirection(layoutDirection: LayoutDirection) {
        dialogLayout.layoutDirection = when (layoutDirection) {
            LayoutDirection.Ltr -> android.util.LayoutDirection.LTR
            LayoutDirection.Rtl -> android.util.LayoutDirection.RTL
        }
    }

    // TODO(b/159900354): Make the Android Dialog full screen and the scrim fully transparent

    fun setContent(parentComposition: CompositionContext, children: @Composable () -> Unit) {
        dialogLayout.setContent(parentComposition, children)
    }

    private fun setSecurePolicy(securePolicy: SecureFlagPolicy) {
        val secureFlagEnabled =
            securePolicy.shouldApplySecureFlag(composeView.isFlagSecureEnabled())
        window!!.setFlags(
            if (secureFlagEnabled) {
                WindowManager.LayoutParams.FLAG_SECURE
            } else {
                WindowManager.LayoutParams.FLAG_SECURE.inv()
            },
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun updateParameters(
        onDismissRequest: () -> Unit,
        properties: SystemDialogProperties,
        layoutDirection: LayoutDirection
    ) {
        this.onDismissRequest = onDismissRequest
        this.properties = properties
        setSecurePolicy(properties.securePolicy)
        setLayoutDirection(layoutDirection)
        dialogLayout.usePlatformDefaultWidth = properties.usePlatformDefaultWidth
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            @OptIn(ExperimentalComposeUiApi::class)
            if (properties.decorFitsSystemWindows) {
                window?.setSoftInputMode(defaultSoftInputMode)
            } else {
                @Suppress("DEPRECATION")
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
        }
    }

    fun disposeComposition() {
        dialogLayout.disposeComposition()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = super.onTouchEvent(event)
        if (result && properties.dismissOnClickOutside) {
            onDismissRequest()
        }

        return result
    }

    override fun cancel() {
        // Prevents the dialog from dismissing itself
        return
    }
}

@Composable
private fun SystemDialogLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.fastMap { it.measure(constraints) }
        val width = placeables.fastMaxBy { it.width }?.width ?: constraints.minWidth
        val height = placeables.fastMaxBy { it.height }?.height ?: constraints.minHeight
        layout(width, height) {
            placeables.fastForEach { it.placeRelative(0, 0) }
        }
    }
}
