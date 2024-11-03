package com.funny.translation

import android.annotation.SuppressLint
import android.provider.Settings
import com.funny.translation.kmp.appCtx

@SuppressLint("HardwareIds")
internal actual fun getDid(): String {
    return Settings.Secure.getString(appCtx.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
}

internal actual fun getVersionName(): String {
    return appCtx.packageManager.getPackageInfo(appCtx.packageName, 0).versionName ?: ""
}

internal actual fun getVersionCode(): Int {
    return appCtx.packageManager.getPackageInfo(appCtx.packageName, 0).versionCode ?: 0
}