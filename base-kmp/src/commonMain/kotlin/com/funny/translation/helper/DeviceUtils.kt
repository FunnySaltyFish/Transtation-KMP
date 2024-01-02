package com.funny.translation.helper

expect object DeviceUtils {
    fun is64Bit(): Boolean
    fun isMute(): Boolean
    fun getSystemVolume(): Int
}