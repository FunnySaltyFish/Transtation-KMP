package com.funny.translation.kmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.funny.translation.helper.SimpleAction
import moe.tlaster.precompose.navigation.NavOptions
import java.util.LinkedList


actual object ActivityManager {
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
        val activity = allActivities[activityClass] ?: return
        activity.windowShowState.value = true
//        activity.onBack = { result: DataType ->
//            activity.windowShowState.value = false
//            onBack(result)
//        }
    }


    inline fun <reified T: KMPActivity> findActivity(): T? {
        return allActivities[T::class.java] as? T
    }
}

interface WindowHolderScope

@Composable
inline fun <reified T: KMPActivity> WindowHolderScope.addWindow(
    windowState: WindowState,
    show: Boolean,
    noinline onCloseRequest: SimpleAction,
    crossinline content: @Composable () -> Unit
) {
    val activity = remember {
        val activityClass = T::class.java
        ActivityManager.findActivity<T>() ?: KMPActivity().also {
            it.windowShowState.value = show
            it.windowState = windowState
            ActivityManager.allActivities[activityClass] =
                activityClass.getConstructor().newInstance()
        }
    }
    if (activity.windowShowState.value) {
        CompositionLocalProvider(LocalKMPContext provides activity) {
            Window(state = activity.windowState, onCloseRequest = onCloseRequest) {
                content()
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

private val WindowHolderScopeImpl = object : WindowHolderScope { }