package com.funny.translation.helper

import java.security.MessageDigest

val String.md5: String
    get() {
        // 创建MessageDigest对象
        val digest = MessageDigest.getInstance("MD5")
        // 对明文进行加密
        val temp = digest.digest(this.toByteArray())
        // 准备StringBuilder用于保存结果
        val builder = StringBuilder()
        // 遍历字节数组, 一个字节转换为长度为2的字符串
        for (b in temp) {
            // 去除负数
            val s = Integer.toHexString(b.toInt() and (0xff))
            // 补零
            if (s.length == 1) {
                builder.append(0)
            }
            builder.append(s)
        }
        return builder.toString()
    }

val String.trimLineStart
    get() = this.splitToSequence("\n").map { it.trim() }.joinToString("\n")

fun String.safeSubstring(start: Int, end: Int = length) = substring(start, minOf(end, length))

fun String.formatBraceStyle(vararg items: Pair<String, Any>): String {
    var txt = this
    items.forEach {
        txt = txt.replace("{${it.first}}", it.second.toString())
    }
    return txt
}

fun String.formatQueryStyle(vararg items: Pair<String, Any>): String {
    var txt = this
    items.forEach {
        txt = txt.replace("{${it.first}}", it.second.toString())
    }
    return txt
}

/** 从字符串中提取JSON
 *
 * @receiver String
 * @return String
 */
fun String.extractJSON(): String {
    val start = indexOf("{")
    val end = lastIndexOf("}")
    return safeSubstring(start, end + 1)
}

/**
 * 获取 uri 或者 filepath 的 suffix，不带 .
 * 找不到则返回空字符串
 */
fun String.extractSuffix(): String {
    val index = lastIndexOf(".")
    return if (index == -1) "" else safeSubstring(index + 1)
}