package com.funny.translation.helper

object DataSaverUtils {
    fun saveData(key: String, value: Any) {}
    fun <T> readData(key: String, defaultValue: T): T {
        return defaultValue
    }
}