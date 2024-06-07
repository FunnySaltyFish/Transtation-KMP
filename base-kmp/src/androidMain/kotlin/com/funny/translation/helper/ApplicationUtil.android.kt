package com.funny.translation.helper

import android.content.Intent
import com.funny.translation.kmp.appCtx

actual object ApplicationUtil {
    actual fun restartApp() {
        val context = appCtx
        // restart App
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}