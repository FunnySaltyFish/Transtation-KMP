package com.funny.translation.translate.utils

import com.funny.translation.Consts
import com.funny.translation.kmp.appCtx
import com.funny.translation.kmp.openAssetsFile
import com.funny.translation.translate.TranslationException


object FunnyBiggerText {
    private const val ZK16 = "HZK16"
    private lateinit var arr: Array<BooleanArray>
    var fillChar = ""
    var all_16_32 = 16
    var all_2_4 = 2
    var all_32_128 = 32
    @Throws(TranslationException::class)
    fun drawWideString(str: String): String {
        var data: ByteArray? = null
        var code: IntArray? = null
        var byteCount: Int
        var lCount: Int
        arr = Array(all_16_32) { BooleanArray(all_16_32 * 2) }
        val sb = StringBuilder()
        for (i in 0 until str.length) {
            val curChar: Char = if (fillChar == "") {
                str[i]
            } else {
                fillChar[0]
            }
            if (str[i].code < 0x80) {
                continue
            }
            code = getByteCode(str.substring(i, i + 1))
            data = read(code[0], code[1])
            byteCount = 0
            for (line in 0 until all_16_32) {
                lCount = 0
                for (k in 0 until all_2_4) {
                    for (j in 0..7) {
                        if (data[byteCount].toInt() shr 7 - j and 0x1 == 1) {
                            arr[line][lCount * 2] = true
                            sb.append(curChar)
                        } else {
                            arr[line][lCount * 2] = false
                            sb.append('　')
                        }
                        sb.append('　')
                        lCount++
                    }
                    byteCount++
                }
                sb.append("\n")
            }
        }
        return sb.toString()
    }

    @Throws(TranslationException::class)
    fun drawMiddleString(str: String): String {
        var data: ByteArray? = null
        var code: IntArray? = null
        var byteCount: Int
        var lCount: Int
        arr = Array(all_16_32) { BooleanArray(all_16_32 * 2) }
        val sb = StringBuilder()
        for (i in 0 until str.length) {
            var curChar: Char
            curChar = if (fillChar == "") {
                str[i]
            } else {
                fillChar[0]
            }
            if (str[i].code < 0x80) {
                continue
            }
            code = getByteCode(str.substring(i, i + 1))
            data = read(code[0], code[1])
            byteCount = 0
            for (line in 0 until all_16_32) {
                lCount = 0
                for (k in 0 until all_2_4) {
                    for (j in 0..7) {
                        if (data[byteCount].toInt() shr 7 - j and 0x1 == 1) {
                            arr[line][lCount * 2] = true
                            sb.append(curChar)
                        } else {
                            arr[line][lCount * 2] = false
                            sb.append('　')
                        }
                        sb.append(' ')
                        lCount++
                    }
                    byteCount++
                }
                sb.append("\n")
            }
        }
        return sb.toString()
    }

    @Throws(TranslationException::class)
    fun drawNarrowString(str: String): String {
        var data: ByteArray? = null
        var code: IntArray? = null
        var byteCount: Int
        var lCount: Int

        //arr = new boolean[all_16_32][all_16_32*2];
        val sb = StringBuilder()
        for (i in 0 until str.length) {
            var curChar: Char
            curChar = if (fillChar == "") {
                str[i]
            } else {
                fillChar[0]
            }
            if (str[i].code < 0x80) {
                continue
            }
            code = getByteCode(str.substring(i, i + 1))
            data = read(code[0], code[1])
            byteCount = 0
            for (line in 0 until all_16_32) {
                lCount = 0
                for (k in 0 until all_2_4) {
                    for (j in 0..7) {
                        if (data[byteCount].toInt() shr 7 - j and 0x1 == 1) {
                            sb.append(curChar)
                        } else {
                            sb.append('　')
                        }
                        lCount++
                    }
                    byteCount++
                    //System.out.println("byteCount is :"+byteCount);
                }
                //System.out.println();
                sb.append("\n")
            }
        }
        return sb.toString()
    }

    @Throws(TranslationException::class)
    internal fun read(areaCode: Int, posCode: Int): ByteArray {
        var data: ByteArray? = null
        try {
            val area = areaCode - 0xa0
            val pos = posCode - 0xa0
            val `in` = appCtx.openAssetsFile(ZK16)
            val offset = (all_32_128 * ((area - 1) * 94 + pos - 1)).toLong()
            `in`.skip(offset)
            data = ByteArray(all_32_128)
            `in`.read(data, 0, all_32_128)
            `in`.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw TranslationException(Consts.ERROR_UNKNOWN)
        }
        return data
    }

    @Throws(TranslationException::class)
    internal fun getByteCode(str: String): IntArray {
        val byteCode = IntArray(2)
        try {
//            val charsets = java.nio.charset.Charset.availableCharsets()
//            Log.d("FunnyBiggerText", "charsets: $charsets")
            val data = str.toByteArray(
                kotlin.runCatching {
                    charset("GBK")
                }.getOrDefault(charset("GB2312"))
            )
            byteCode[0] = if (data[0] < 0) 256 + data[0] else data[0].toInt()
            byteCode[1] = if (data[1] < 0) 256 + data[1] else data[1].toInt()
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw TranslationException(Consts.ERROR_UNKNOWN)
        }
        return byteCode
    }
}