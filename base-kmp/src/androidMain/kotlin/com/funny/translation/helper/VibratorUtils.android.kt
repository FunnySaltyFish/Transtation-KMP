package com.funny.translation.helper

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.funny.translation.kmp.appCtx

actual object VibratorUtils {
    private val vibrator by lazy{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = appCtx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appCtx.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    actual fun vibrate(time: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE))
        } else vibrator.vibrate(time)
    }

    actual fun cancel() {
        vibrator.cancel()
    }
}