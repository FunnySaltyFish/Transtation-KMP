package com.funny.translation.translate.activity

import android.os.Bundle
import android.os.Process
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.funny.translation.BaseActivity
import kotlinx.coroutines.launch
import java.io.File
import kotlin.system.exitProcess


actual class ErrorDialogActivity : BaseActivity() {
    actual var crashMessage : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crashMessage = intent.getStringExtra("CRASH_MESSAGE")
        lifecycleScope.launch {
            if (crashMessage != null) {
                saveCrashMessage(crashMessage!!)
            }
        }
        
        setContent {
            crashMessage?.let {
                ErrorDialog(it, ::destroy)
            }
        }
    }

    actual fun saveCrashMessage(msg: String){
        val file = this.getExternalFilesDir("crash_logs")
        if (!file?.exists()!!) {
            file.mkdirs()
        }
        // 文件名： CrashLog_时间.txt
        val fileName = "CrashLog_" + System.currentTimeMillis() + ".txt"
        val outputFile = File(file, fileName)
        outputFile.writeText(msg)
    }

    actual fun destroy() {
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }
}
