package com.funny.translation.helper

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

actual object BitmapUtil {

    actual fun getBitmapFromUri(ctx: Context, targetWidth: Int, targetHeight: Int, maxSize: Long, uri: String): ByteArray? {
        // TODO 实现获取位图逻辑...

        return null
    }

    actual fun getImageSizeFromUri(ctx: Context, uri: String): Pair<Int, Int> {
        // 实现获取图片大小逻辑...

        return Pair(-1, -1)
    }

    actual fun saveBitmap(bytes: ByteArray, imagePath: String) {
        val file = File(imagePath)
        if (file.exists()) {
            file.delete()
        }
        try {
            file.createNewFile()
            val fos = FileOutputStream(file)
            fos.write(bytes)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
