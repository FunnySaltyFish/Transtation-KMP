package com.funny.translation.helper

import com.eygraber.uri.Uri
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

actual object BitmapUtil {
    actual fun compressImage(bytes: ByteArray?, width: Int, height: Int, maxSize: Long): ByteArray {
        bytes ?: return byteArrayOf()

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val intArray = IntArray(bytes.size)

//        image.setRGB(0, 0, width, height, bytes 0, width)

        // 实现压缩逻辑...
        // TODO
        return byteArrayOf()
    }

    actual fun getBitmapFromUri(ctx: Context, targetWidth: Int, targetHeight: Int, maxSize: Long, uri: Uri): ByteArray? {
        // TODO 实现获取位图逻辑...

        return null
    }

    actual fun getImageSizeFromUri(ctx: Context, uri: Uri): Pair<Int, Int> {
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
