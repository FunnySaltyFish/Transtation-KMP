package com.funny.translation.helper

import android.content.Context
import android.media.AudioManager
import android.os.Build
import com.funny.translation.kmp.appCtx

actual object DeviceUtils {

    actual fun is64Bit(): Boolean {
        return Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()
    }

    // 获取系统当前音量
    actual fun getSystemVolume(): Int {
        val audioManager = appCtx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    // 判断是否静音
    actual fun isMute() = getSystemVolume() == 0
}