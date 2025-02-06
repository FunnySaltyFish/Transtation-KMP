package com.funny.translation

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.view.Gravity
import com.funny.translation.helper.DeviceUtils
import com.funny.translation.helper.LocaleUtils
import com.funny.translation.kmp.ActivityManager
import com.hjq.toast.Toaster
import com.tencent.mmkv.MMKV
import java.util.*
import kotlin.properties.Delegates


open class BaseApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        val context = base?.let {
            LocaleUtils.init(it)
            MMKV.initialize(base)
            val locale = LocaleUtils.getLocaleDirectly()
            LocaleUtils.getWarpedContext(it, locale)
        }
        super.attachBaseContext(context)
    }

    override fun onCreate() {
        super.onCreate()

        ctx = this

        Toaster.init(this)
        Toaster.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 260)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks{
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is BaseActivity) {
                    ActivityManager.addActivity(activity)
                }
            }
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (activity is BaseActivity) {
                    ActivityManager.removeActivity(activity)
                }
            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }
        })
    }

    companion object {
        var ctx : Application by Delegates.notNull()
        val resources: Resources get() = ctx.resources

        @Throws(PackageManager.NameNotFoundException::class)
        fun getLocalPackageInfo(): PackageInfo? {
            return ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.GET_CONFIGURATIONS)
        }

        fun getCurrentActivity() = ActivityManager.currentActivity()

        private const val TAG = "BaseApplication"
    }
}