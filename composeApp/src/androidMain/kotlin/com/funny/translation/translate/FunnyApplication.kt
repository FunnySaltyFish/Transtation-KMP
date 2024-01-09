package com.funny.translation.translate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import com.funny.translation.BaseApplication
import com.funny.translation.helper.DeviceUtils
import com.funny.translation.sign.SignUtils
import com.funny.translation.translate.utils.FunnyUncaughtExceptionHandler
import com.funny.translation.translate.utils.SortResultUtils
import com.funny.translation.translate.utils.initTypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class FunnyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        ctx = this

        if (DeviceUtils.is64Bit()) {
            // 仅在 64 位时加载
            System.loadLibrary("monet")
        }

        FunnyUncaughtExceptionHandler.init(ctx)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createScreenCaptureNotificationChannel()
        }

        CoroutineScope(Dispatchers.IO).launch {
            SignUtils.loadJs()
            SortResultUtils.init()
        }

        initTypeConverters()
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

