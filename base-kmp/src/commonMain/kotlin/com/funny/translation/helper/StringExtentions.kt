package com.funny.translation.helper

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.funny.cmaterialcolors.MaterialColors
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
    val jsonRegex = """```(json)?([\s\S]*?)```""".toRegex()
    val matchResult = jsonRegex.find(this)
    return if (matchResult != null) {
        matchResult.groupValues[2].trim()
    } else {
        val regex2 = """\[[\s\S]*]|\{[\s\S]*\}""".toRegex()
        regex2.find(this)?.value ?: "{}"
    }
}

/**
 * 获取 uri 或者 filepath 的 suffix，不带 .
 * 找不到则返回空字符串
 */
fun String.extractSuffix(): String {
    val index = lastIndexOf(".")
    return if (index == -1) "" else safeSubstring(index + 1)
}

/**
 * 根据 search 构建 AnnotatedString
 */
@Composable
fun buildSearchAnnotatedString(
    content: String,
    search: String,
) = buildAnnotatedString {
    if (search.isEmpty()) {
        append(content)
        return@buildAnnotatedString
    }
    var start = 0
    var end = 0
    while (end < content.length) {
        end = content.indexOf(search, start, ignoreCase = true)
        if (end == -1) {
            append(content.substring(start))
            break
        }
        append(content.substring(start, end))
        withStyle(
            LocalTextStyle.current.copy(
                color = MaterialColors.OrangeA400,
                fontWeight = FontWeight.Bold,
            ).toSpanStyle()
        ) {
            append(content.substring(end, end + search.length))
        }
        start = end + search.length
    }
}