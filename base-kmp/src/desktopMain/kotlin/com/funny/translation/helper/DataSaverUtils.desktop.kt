package com.funny.translation.helper

import com.funny.data_saver.core.DataSaverInterface
import java.io.FileReader
import java.io.FileWriter
import java.util.Properties

class DataSaverProperties(private val filePath: String) : DataSaverInterface() {
    private val properties = Properties()

    init {
        try {
            FileReader(filePath).use { reader ->
                properties.load(reader)
            }
        } catch (e: Exception) {
            // 处理文件不存在等异常
        }
    }

    private fun saveProperties() {
        FileWriter(filePath).use { writer ->
            properties.store(writer, null)
        }
    }

    override fun <T> saveData(key: String, data: T) {
        properties[key] = data.toString()
        saveProperties()
    }

    override fun <T> readData(key: String, default: T): T {
        val value = properties.getProperty(key) ?: return default
        return when (default) {
            is Int -> value.toIntOrNull() ?: default
            is Long -> value.toLongOrNull() ?: default
            is Boolean -> value.toBooleanStrictOrNull() ?: default
            is Double -> value.toDoubleOrNull() ?: default
            is Float -> value.toFloatOrNull() ?: default
            is String -> value
            else -> throwError("read", default)
        } as T
    }

    override fun remove(key: String) {
        properties.remove(key)
        saveProperties()
    }

    override fun contains(key: String): Boolean {
        return properties.containsKey(key)
    }
}

actual val DataSaverUtils: DataSaverInterface by lazy {
    DataSaverProperties(CacheManager.baseDir.resolve("data_saver.properties").absolutePath)
}