package com.funny.translation.helper

expect object VibratorUtils {
    fun vibrate(time: Long = 100)

    fun cancel()
}