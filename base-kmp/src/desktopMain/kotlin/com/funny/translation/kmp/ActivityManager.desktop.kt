package com.funny.translation.kmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.funny.translation.helper.SimpleAction
import moe.tlaster.precompose.navigation.NavOptions
import java.util.LinkedList

actual object ActivityManager {
    actual val activityStack: MutableList<KMPActivity> = LinkedList<KMPActivity>()

    actual fun addActivity(activity: KMPActivity) {
        activityStack.add(activity)
    }

    actual fun removeActivity(activity: KMPActivity) {
        activityStack.remove(activity)
    }

    actual fun currentActivity(): KMPActivity? {
        return activityStack.lastOrNull()
    }

    actual fun <Result> start(
        activity: KMPActivity,
        data: Map<String, Any?>,
        options: NavOptions,
        onBack: (result: Map<String, Any?>?) -> Unit
    ) {
        activity.windowShowState.value = true
        activity.onBack = { result: DataType ->
            activity.windowShowState.value = false
            onBack(result)
        }
    }
}

interface WindowHolderScope {
    @Composable
    fun addWindow(activity: KMPActivity, onCloseRequest: SimpleAction, content: @Composable () -> Unit)
}

object WindowHolderScopeImpl : WindowHolderScope {
    @Composable
    override fun addWindow(activity: KMPActivity, onCloseRequest: SimpleAction, content: @Composable () -> Unit) {
        if (activity.windowShowState.value) {
            Window(state = activity.state, onCloseRequest = onCloseRequest) {
                content()
            }
        }
    }
}

@Composable
internal fun WindowHolder(
    block: @Composable WindowHolderScope.() -> Unit
) {
    WindowHolderScopeImpl.block()
}


@Composable
private fun WindowTest() {
    WindowHolder {
        addWindow(activity = KMPActivity(WindowState()), onCloseRequest = {  }) {

        }
    }
}