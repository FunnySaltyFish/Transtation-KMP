package com.funny.translation.translate.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.text.TextUtils
import com.funny.translation.BaseApplication.Companion.getLocalPackageInfo
import kotlin.system.exitProcess

object FunnyUncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
    private var applicationContext: Context? = null
    private var crashing = false
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    fun init(ctx: Context) {
        applicationContext = ctx.applicationContext
        crashing = false
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        if (crashing) return
        crashing = true
        throwable.printStackTrace()
        if (!handleCrashByMe(throwable) && mDefaultHandler != null) {
            // 系统处理
            mDefaultHandler!!.uncaughtException(thread, throwable)
        }
        destroy()
    }

    private fun handleCrashByMe(ex: Throwable?): Boolean {
        if (ex == null) return false
        try {
            println("接管了应用的报错！")
            val intent = Intent()
            // TODO 加回来
            // intent.setClass(applicationContext, ErrorDialogActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("CRASH_MESSAGE", getCrashReport(ex))
            applicationContext!!.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun getCrashReport(ex: Throwable?): String {
        val exceptionStr = StringBuilder()
        val packageInfo = getLocalPackageInfo()
        return if (packageInfo != null) {
            if (ex != null) {
                //app版本信息
                exceptionStr.append("App Version：").append(packageInfo.versionName)
                exceptionStr.append("_").append(packageInfo.versionCode).append("\n")

                //手机系统信息
                exceptionStr.append("OS Version：").append(Build.VERSION.RELEASE)
                exceptionStr.append("_")
                exceptionStr.append(Build.VERSION.SDK_INT).append("\n")

                //手机制造商
                exceptionStr.append("Vendor: ").append(Build.MANUFACTURER).append("\n")

                //手机型号
                exceptionStr.append("Model: ").append(Build.MODEL).append("\n")
                var errorStr = ex.localizedMessage
                if (TextUtils.isEmpty(errorStr)) {
                    errorStr = ex.message
                }
                if (TextUtils.isEmpty(errorStr)) {
                    errorStr = ex.toString()
                }
                exceptionStr.append("Exception: ").append(errorStr).append("\n")
                val elements = ex.stackTrace
                for (element in elements) {
                    exceptionStr.append(element.toString()).append("\n")
                }
            } else {
                exceptionStr.append("no exception. Throwable is null\n")
            }
            exceptionStr.toString()
        } else {
            ""
        }
    }

    private fun destroy() {
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }
}