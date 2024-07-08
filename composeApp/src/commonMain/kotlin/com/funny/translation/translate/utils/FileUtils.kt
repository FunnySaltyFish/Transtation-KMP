package com.funny.translation.translate.utils

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