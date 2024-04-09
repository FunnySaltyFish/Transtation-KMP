package com.funny.translation.translate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.funny.data_saver.core.DataSaverConverter
import com.funny.translation.BaseApplication
import com.funny.translation.codeeditor.ui.editor.EditorSchemes
import com.funny.translation.translate.utils.FunnyUncaughtExceptionHandler
import com.funny.translation.translate.utils.InitUtil
import com.funny.translation.translate.utils.initCommon
import kotlinx.coroutines.runBlocking
import kotlin.properties.Delegates

class FunnyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        ctx = this

        FunnyUncaughtExceptionHandler.init(ctx)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createScreenCaptureNotificationChannel()
        }

        runBlocking {
            InitUtil.initCommon()

            DataSaverConverter.registerTypeConverters<EditorSchemes>(
                save = { it.name },
                restore = { EditorSchemes.valueOf(it) }
            )

            DataSaverConverter.registerTypeConverters<Uri?>(
                save = { it.toString() },
                restore = { if (it == "null") null else Uri.parse(it) }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createScreenCaptureNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // Create the channel for the notification
        val screenCaptureChannel = NotificationChannel(SCREEN_CAPTURE_CHANNEL_ID, SCREEN_CAPTURE_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        // Set the Notification Channel for the Notification Manager.
        notificationManager.createNotificationChannel(screenCaptureChannel)
    }

    companion object {
        var ctx by Delegates.notNull<FunnyApplication>()
        val resources: Resources get() = ctx.resources
        const val TAG = "FunnyApplication"

        internal const val SCREEN_CAPTURE_CHANNEL_ID = "CID_Screen_Capture"
        private const val SCREEN_CAPTURE_CHANNEL_NAME = "CNAME_Screen_Capture"
    }
}

