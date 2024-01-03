package com.funny.translation.helper

import android.content.Intent
import com.funny.translation.kmp.appCtx

actual object ApplicationUtil {
    actual fun restartApp() {
        val context = appCtx
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}