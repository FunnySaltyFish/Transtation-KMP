package com.funny.translation.kmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.funny.translation.helper.SimpleAction
import moe.tlaster.precompose.navigation.NavOptions
import java.util.LinkedList

class KMPActivityImpl(val state: WindowState) : KMPContext(), KMPActivity {
    val windowShowState = mutableStateOf(false)
}

actual object ActivityManager {
    val allActivityImpl = hashMapOf<Class<out KMPActivity>, KMPActivityImpl>()
    val allActivities = hashMapOf<Class<out KMPActivity>, KMPActivity>()

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
        val activity = allActivityImpl[activityClass] ?: return
        activity.windowShowState.value = true
//        activity.onBack = { result: DataType ->
//            activity.windowShowState.value = false
//            onBack(result)
//        }
    }

    fun findActivityImpl(activityClass: Class<out KMPActivity>): KMPActivityImpl? {
        return allActivityImpl[activityClass]
    }

    inline fun <reified T: KMPActivity> findActivity(): T? {
        return allActivities[T::class.java] as? T
    }
}

interface WindowHolderScope {
    @Composable
    fun addWindow(
        activityClass: Class<out KMPActivity>,
        windowState: WindowState,
        show: Boolean,
        onCloseRequest: SimpleAction,
        content: @Composable () -> Unit
    )
}

object WindowHolderScopeImpl : WindowHolderScope {
    @Composable
    override fun addWindow(
        activityClass: Class<out KMPActivity>,
        windowState: WindowState,
        show: Boolean,
        onCloseRequest: SimpleAction,
        content: @Composable () -> Unit
    ) {
        val activity = ActivityManager.findActivityImpl(activityClass) ?:
            KMPActivityImpl(windowState).also {
                it.windowShowState.value = show
                ActivityManager.allActivityImpl[activityClass] = it
                ActivityManager.allActivities[activityClass] = activityClass.getConstructor().newInstance()
            }
        if (activity.windowShowState.value) {
            CompositionLocalProvider(LocalKMPContext provides activity) {
                Window(state = activity.state, onCloseRequest = onCloseRequest) {
                    content()
                }
            }
        }
    }
}

@Composable
fun WindowHolder(
    block: @Composable WindowHolderScope.() -> Unit
) {
    WindowHolderScopeImpl.block()
}


@Composable
private fun WindowTest() {
    WindowHolder {
    }
}