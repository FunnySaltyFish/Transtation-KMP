package com.funny.translation.translate.activity

import com.funny.translation.BaseActivity
import com.funny.translation.helper.CacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.system.exitProcess

const val KEY_CRASH_MESSAGE = "KEY_CRASH_MESSAGE"

actual class ErrorDialogActivity : BaseActivity() {
    internal actual var crashMessage: String? = null

    override fun onShow() {
        super.onShow()
    }

    override fun onStart() {
        super.onStart()
        crashMessage = data?.get(KEY_CRASH_MESSAGE) as? String

        crashMessage?.let {
            CoroutineScope(Dispatchers.IO).launch {
                saveCrashMessage(it)
            }
        }
    }

    actual fun saveCrashMessage(msg: String) {
        val file = CacheManager.cacheDir.resolve("crash_logs")
        if (!file.exists()) {
            file.mkdirs()
        }
        // 文件名： CrashLog_时间.txt
        val fileName = "CrashLog_" + System.currentTimeMillis() + ".txt"
        val outputFile = File(file, fileName)
        outputFile.writeText(msg)
    }

    internal actual fun destroy() {
        exitProcess(0)
    }
}