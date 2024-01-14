package com.funny.translation.kmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.funny.translation.BaseActivity
import com.funny.translation.helper.Log
import com.funny.translation.helper.SimpleAction
import com.funny.translation.ui.App
import moe.tlaster.precompose.navigation.NavOptions
import java.util.LinkedList


actual object ActivityManager {
    val allActivities = hashMapOf<Class<out BaseActivity>, BaseActivity>()

    actual val activityStack: MutableList<BaseActivity> = LinkedList<BaseActivity>()

    actual fun addActivity(activity: BaseActivity) {
        activityStack.add(activity)
    }

    actual fun removeActivity(activity: BaseActivity) {
        activityStack.remove(activity)
    }

    actual fun currentActivity(): BaseActivity? {
        return activityStack.lastOrNull()
    }

    actual fun start(
        targetClass: Class<out BaseActivity>,
        data: MutableMap<String, Any?>,
        options: NavOptions,
        onBack: (result: Map<String, Any?>?) -> Unit
    ) {
        val activity = allActivities[targetClass] ?: return
        activity.data = data
        activity.windowShowState.value = true

        activity.onStart()
        Log.d("ActivityManager", "start: $activity")
    }


    inline fun <reified T: BaseActivity> findActivity(): T? {
        return allActivities[T::class.java] as? T
    }

    inline fun <reified T: BaseActivity> hide() {
        findActivity<T>()?.windowShowState?.value = false
    }
}

interface WindowHolderScope

@Composable
inline fun <reified T: BaseActivity> WindowHolderScope.addWindow(
    windowState: WindowState,
    show: Boolean = false,
    noinline onCloseRequest: SimpleAction = {},
    crossinline content: @Composable (T) -> Unit
) {
    val activity: T = remember {
        val activityClass = T::class.java
        ActivityManager.findActivity<T>() ?: activityClass.getConstructor().newInstance().also {
            it.windowShowState.value = show
            it.windowState = windowState
            ActivityManager.allActivities[activityClass] = it
        } as T
    }

    val showWindow by activity.windowShowState

    if (showWindow) {
        key(activity) {
            CompositionLocalProvider(
                LocalKMPContext provides activity,
            ) {
                LaunchedEffect(activity) {
                    activity.onShow()
                }
                Window(state = activity.windowState, onCloseRequest = {
                    Log.d("WindowHolder", "onCloseRequest: $activity")
                    onCloseRequest()
                    activity.windowShowState.value = false
                }) {
                    App {
                        content(activity)
                    }
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

private val WindowHolderScopeImpl = object : WindowHolderScope { }