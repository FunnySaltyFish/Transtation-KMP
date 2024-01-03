package com.funny.translation.kmp

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.funny.translation.helper.Log
import moe.tlaster.precompose.navigation.NavOptions
import java.util.LinkedList

actual object ActivityManager {
    private const val TAG = "ActivityManager"
    actual val activityStack: MutableList<KMPActivity> = LinkedList<KMPActivity>()
    private val activityResultLaunchers = mutableMapOf<KMPActivity, ActivityResultLauncher<Intent>>()

    actual fun addActivity(activity: KMPActivity) {
        activityStack.add(activity)
        Log.d(TAG, "【${activity::class.java.simpleName}】 Created! currentStackSize:${activityStack.size}")
    }

    actual fun removeActivity(activity: KMPActivity) {
        activityStack.remove(activity)
        Log.d(TAG, "【${activity::class.java.simpleName}】 Destroyed! currentStackSize:${activityStack.size}")
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
        if (activity !is ComponentActivity) return
        val launcher = activityResultLaunchers[activity] ?: activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            onBack(it.data?.toMap())
        }.also {
            activityResultLaunchers[activity] = it
        }
        launcher.launch(data.toIntent())
    }
}

private fun Intent.toMap(): Map<String, Any?> {
    return mapOf(
        "action" to action,
        "data" to data,
        "extras" to extras,
        "flags" to flags,
    )
}

private fun Map<String, Any?>.toIntent(): Intent {
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