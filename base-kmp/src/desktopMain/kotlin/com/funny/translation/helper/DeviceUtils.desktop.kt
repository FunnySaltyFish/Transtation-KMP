package com.funny.translation.helper

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.FloatControl
import javax.sound.sampled.SourceDataLine

// desktop
// desktop
actual object DeviceUtils {
    actual fun is64Bit(): Boolean {
        val arch = System.getProperty("os.arch")
        return arch.contains("64")
    }

    // 获取系统当前音量
    actual fun getSystemVolume(): Int {
        try {
            val mixerInfo = AudioSystem.getMixerInfo()
            val mixer = AudioSystem.getMixer(mixerInfo[0])

            val line = mixer.getLine(DataLine.Info(SourceDataLine::class.java, AudioFormat(44100f, 16, 2, true, false)))

            if (line is SourceDataLine) {
                line.open()
                return (line.getControl(FloatControl.Type.VOLUME) as FloatControl).value.toInt()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 默认返回 0
        return 0
    }

    // 判断是否静音
    actual fun isMute(): Boolean = getSystemVolume() == 0
}
