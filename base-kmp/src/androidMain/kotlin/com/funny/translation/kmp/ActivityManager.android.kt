package com.funny.translation.kmp

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.funny.translation.BaseActivity
import com.funny.translation.helper.Log
import moe.tlaster.precompose.navigation.NavOptions
import java.util.LinkedList

actual object ActivityManager {
    private const val TAG = "ActivityManager"
    actual val activityStack: MutableList<BaseActivity> = LinkedList<BaseActivity>()
    private val activityResultLaunchers = mutableMapOf<BaseActivity, ActivityResultLauncher<Intent>>()

    actual fun addActivity(activity: BaseActivity) {
        activityStack.add(activity)
        Log.d(TAG, "【${activity::class.java.simpleName}】 Created! currentStackSize:${activityStack.size}")
    }

    actual fun removeActivity(activity: BaseActivity) {
        activityStack.remove(activity)
        Log.d(TAG, "【${activity::class.java.simpleName}】 Destroyed! currentStackSize:${activityStack.size}")
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
        val activity = currentActivity() ?: return
        val launcher = activity.activityResultLauncher
        launcher.launch(data.toIntent().apply {
            setClassName(activity, targetClass.name)
        })
    }
}

fun Intent.toMap(): Map<String, Any?> {
    return mapOf(
        "action" to action,
        "data" to data,
        "extras" to extras,
        "flags" to flags,
    )
}

fun Map<String, Any?>.toIntent(): Intent {
    return Intent().apply {
        action = this@toIntent["action"] as? String
        data = this@toIntent["data"] as? android.net.Uri
        val extras = this@toIntent["extras"] as? DataType
        extras?.entries?.forEach { entry ->
            val (key, value) = entry
            when (value) {
                is String -> putExtra(key, value)
                is Int -> putExtra(key, value)
                is Boolean -> putExtra(key, value)
                is Float -> putExtra(key, value)
                is Double -> putExtra(key, value)
                is Long -> putExtra(key, value)
            }
        }
        flags = this@toIntent["flags"] as? Int ?: 0
    }
}