package com.funny.translation.kmp

import com.eygraber.uri.Uri
import java.nio.file.Files
import java.nio.file.Paths

actual fun Uri.writeText(text: String) {
    try {
        // 将文本写入文件
        Files.write(Paths.get(toFileString()), text.toByteArray())
    } catch (e: Exception) {
        // 处理异常
        e.printStackTrace()
    }
}

actual fun Uri.readText(): String {
    try {
        // 读取文件内容
        return Files.readString(Paths.get(toFileString()))
    } catch (e: Exception) {
        // 处理异常
        e.printStackTrace()
    }
    return ""
}

fun Uri.Companion.fromFile(file: String): Uri {
    return Uri.fromParts("file", file, null)
}

fun Uri.toFileString(): String {
    return this.toString().removePrefix("file://")
}

