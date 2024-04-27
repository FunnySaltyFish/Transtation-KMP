package com.funny.translation.helper

import com.funny.data_saver.core.DataSaverInterface
import com.funny.translation.BuildConfig
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.security.MessageDigest
import java.util.Arrays
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DataSaverProperties(private val filePath: String, private val encryptionKey: String) : DataSaverInterface() {
    private val properties = Properties()
    private val hashedKey = hashKey(encryptionKey)

    init {
        try {
            val f = File(filePath)
            if (!f.exists()) {
                f.parentFile.mkdirs()
                f.createNewFile()
            }
            FileReader(filePath).use { reader ->
                val decryptedReader = BufferedReader(InputStreamReader(CipherInputStream(FileInputStream(filePath), createCipher(Cipher.DECRYPT_MODE))))
                properties.load(decryptedReader)
            }
        } catch (e: FileNotFoundException) {
            // Handle file not found exception
        } catch (e: Exception) {
            // Handle other exceptions
        }
    }

    private fun saveProperties() {
        try {
            val encryptedWriter = BufferedWriter(OutputStreamWriter(CipherOutputStream(FileOutputStream(filePath), createCipher(Cipher.ENCRYPT_MODE))))
            properties.store(encryptedWriter, null)
        } catch (e: Exception) {
            // Handle file write exception
        }
    }

    private fun createCipher(mode: Int): Cipher {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(hashedKey, "AES")
        val ivParameterSpec = IvParameterSpec(Arrays.copyOfRange(hashedKey, 0, 16))
        cipher.init(mode, keySpec, ivParameterSpec)
        return cipher
    }

    private fun hashKey(key: String): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(key.toByteArray())
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
    // 读取 dotenv
    DataSaverProperties(
        filePath = CacheManager.baseDir.resolve("data_saver.properties").absolutePath,
        encryptionKey = BuildConfig.MAGIC_KEY
    )
}