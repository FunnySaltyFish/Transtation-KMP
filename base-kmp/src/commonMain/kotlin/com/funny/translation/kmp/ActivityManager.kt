package com.funny.translation.kmp

import moe.tlaster.precompose.navigation.NavOptions

// KMP Activity Manager, used to manage the activity stack
// in Android, it will manage the android.app.Activity
// in Desktop, it will manage the Window
typealias DataType = Map<String, Any?>

expect object ActivityManager {
    val activityStack : MutableList<KMPActivity>
    fun addActivity(activity: KMPActivity)
    fun removeActivity(activity: KMPActivity)
    fun currentActivity(): KMPActivity?

    fun start(activityClass: Class<out KMPActivity>, data: Map<String, Any?> = emptyMap(), options: NavOptions = NavOptions(), onBack: (result: Map<String, Any?>?) -> Unit = {})
}