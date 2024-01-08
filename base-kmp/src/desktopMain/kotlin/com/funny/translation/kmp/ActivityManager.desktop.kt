package com.funny.translation.kmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.funny.translation.helper.SimpleAction
import moe.tlaster.precompose.navigation.NavOptions
import java.util.LinkedList

class KMPActivityImpl(val state: WindowState) : KMPActivity {
    val windowShowState = mutableStateOf(false)
}

actual object ActivityManager {
    val allActivities = hashMapOf<Class<out KMPActivity>, KMPActivityImpl>()

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

    actual fun start(
        activityClass: Class<out KMPActivity>,
        data: Map<String, Any?>,
        options: NavOptions,
        onBack: (result: Map<String, Any?>?) -> Unit
    ) {
        val activity = allActivities[activityClass] ?: return
        activity.windowShowState.value = true
//        activity.onBack = { result: DataType ->
//            activity.windowShowState.value = false
//            onBack(result)
//        }
    }

    fun findActivityImpl(activityClass: Class<out KMPActivity>): KMPActivityImpl? {
        return allActivities[activityClass]
    }
}

interface WindowHolderScope {
    @Composable
    fun addWindow(activityClass: Class<out KMPActivity>, windowState: WindowState, onCloseRequest: SimpleAction, content: @Composable () -> Unit)
}

object WindowHolderScopeImpl : WindowHolderScope {
    @Composable
    override fun addWindow(activityClass: Class<out KMPActivity>, windowState: WindowState, onCloseRequest: SimpleAction, content: @Composable () -> Unit) {
        val activity = ActivityManager.findActivityImpl(activityClass) ?: KMPActivityImpl(windowState).also { ActivityManager.allActivities[activityClass] = it }
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
    }
}