package com.funny.translation.translate.utils

import com.funny.translation.AppConfig
import com.funny.translation.helper.Context
import com.funny.translation.kmp.ActivityManager
import com.funny.translation.kmp.appCtx
import com.funny.translation.translate.TransActivity
import com.funny.translation.translate.activity.ErrorDialogActivity
import com.funny.translation.translate.activity.KEY_CRASH_MESSAGE
import kotlin.system.exitProcess

object DesktopUncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
    private var applicationContext: Context? = null
    private var crashing = false
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    fun init(ctx: Context) {
        applicationContext = appCtx
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
        ActivityManager.hide<TransActivity>()
//        destroy()
    }

    private fun handleCrashByMe(ex: Throwable?): Boolean {
        if (ex == null) return false
        try {
            println("接管了应用的报错！")
            ActivityManager.start(
                ErrorDialogActivity::class.java,
                hashMapOf(KEY_CRASH_MESSAGE to getCrashReport(ex))
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun getCrashReport(ex: Throwable?): String {
        return buildString {
            // 应用版本信息，需要自定义方式来获取
            append("App Version：").append(AppConfig.versionName).append("_").append(AppConfig.versionCode).append("\n")

            // JVM 版本信息
            append("JVM Version：").append(System.getProperty("java.version")).append("\n")

            // JVM 名称
            append("JVM Name: ").append(System.getProperty("java.vm.name")).append("\n")

            // 操作系统信息
            append("OS Name: ").append(System.getProperty("os.name")).append("\n")
            append("OS Version：").append(System.getProperty("os.version")).append("_")
            append(System.getProperty("os.arch")).append("\n")

            // 异常信息
            var errorStr = ex?.localizedMessage
            if (errorStr.isNullOrEmpty()) {
                errorStr = ex?.message
            }
            if (errorStr.isNullOrEmpty()) {
                errorStr = ex?.toString()
            }
            append("Exception: ").append(errorStr).append("\n")

            // 异常堆栈跟踪
            ex?.stackTrace?.forEach { element ->
                append(element.toString()).append("\n")
            }
        }
    }

    private fun destroy() {
        exitProcess(0)
    }
}