package com.funny.translation.helper

import java.io.File

/**
 * 创建文件，包含创建父目录
 * @receiver File
 * @return Boolean
 */
fun File.createFileIfNotExist(): Boolean {
    val file = this
    if (!file.exists()) {
        val parentFile = file.parentFile
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        return file.createNewFile()
    }
    return true
}

fun File.createParentDirIfNotExist(): Boolean {
    val parentFile = this.parentFile ?: return false
    // mkdirs 中已经判断过 exists 了
    return parentFile.mkdirs()
}

fun File.fileMD5(): String? {
    return try {
        if (!exists()) return ""
        val digest = java.security.MessageDigest.getInstance("MD5")
        digest.update(readBytes())
        val bytes = digest.digest()
        val sb = java.lang.StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        sb.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}