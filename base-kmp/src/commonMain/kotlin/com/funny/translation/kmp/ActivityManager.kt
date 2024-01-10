package com.funny.translation.kmp

import com.funny.translation.BaseActivity
import moe.tlaster.precompose.navigation.NavOptions

// KMP Activity Manager, used to manage the activity stack
// in Android, it will manage the android.app.Activity
// in Desktop, it will manage the Window
typealias DataType = Map<String, Any?>

expect object ActivityManager {
    val activityStack : MutableList<BaseActivity>
    fun addActivity(activity: BaseActivity)
    fun removeActivity(activity: BaseActivity)
    fun currentActivity(): BaseActivity?

    fun start(targetClass: Class<out BaseActivity>, data: MutableMap<String, Any?> = hashMapOf(), options: NavOptions = NavOptions(), onBack: (result: Map<String, Any?>?) -> Unit = {})
}