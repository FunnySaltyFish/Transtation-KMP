package com.funny.translation.helper

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.imageio.ImageIO

// GPT-4o 写的
actual object BitmapUtil {

    actual fun getBitmapFromUri(ctx: Context, targetWidth: Int, targetHeight: Int, maxSize: Long, uri: String): ByteArray? {
        val file = File(uri.removePrefix("file://"))
        if (!file.exists()) return null

        try {
            val originalImage = ImageIO.read(file)
            val scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)

            val bufferedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
            val graphics2D = bufferedImage.createGraphics()
            graphics2D.drawImage(scaledImage, 0, 0, null)
            graphics2D.dispose()

            val byteArrayOutputStream = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.close()

            return if (imageBytes.size <= maxSize) imageBytes else null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    actual fun getImageSizeFromUri(ctx: Context, uri: String): Pair<Int, Int> {
        val file = File(uri.removePrefix("file://"))
        if (!file.exists()) return Pair(-1, -1)

        try {
            val image = ImageIO.read(file)
            return Pair(image.width, image.height)
        } catch (e: IOException) {
            e.printStackTrace()
            return Pair(-1, -1)
        }
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

    actual fun compressImage(
        bytes: ByteArray?,
        maxWidth: Int,
        maxHeight: Int,
        maxSize: Long
    ): ByteArray {
        if (bytes == null) return byteArrayOf()

        try {
            val inputStream = bytes.inputStream()
            val originalImage = ImageIO.read(inputStream)
            inputStream.close()

            val scaledImage = originalImage.getScaledInstance(maxWidth, maxHeight, Image.SCALE_SMOOTH)

            val bufferedImage = BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB)
            val graphics2D = bufferedImage.createGraphics()
            graphics2D.drawImage(scaledImage, 0, 0, null)
            graphics2D.dispose()

            val byteArrayOutputStream = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream)
            var compressedBytes = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.close()

            if (compressedBytes.size > maxSize) {
                val compressionRatio = maxSize.toDouble() / compressedBytes.size
                val newWidth = (maxWidth * compressionRatio).toInt()
                val newHeight = (maxHeight * compressionRatio).toInt()

                val newScaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)
                val newBufferedImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
                val newGraphics2D = newBufferedImage.createGraphics()
                newGraphics2D.drawImage(newScaledImage, 0, 0, null)
                newGraphics2D.dispose()

                val newByteArrayOutputStream = ByteArrayOutputStream()
                ImageIO.write(newBufferedImage, "jpg", newByteArrayOutputStream)
                compressedBytes = newByteArrayOutputStream.toByteArray()
                newByteArrayOutputStream.close()
            }

            return compressedBytes
        } catch (e: IOException) {
            e.printStackTrace()
            return byteArrayOf()
        }
    }

    actual fun getBitmapFromUri(uri: String): ByteArray? {
        val file = File(uri.removePrefix("file://"))
        if (!file.exists()) return null

        try {
            val originalImage = ImageIO.read(file)
            val byteArrayOutputStream = ByteArrayOutputStream()
            ImageIO.write(originalImage, "jpg", byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.close()

            return imageBytes
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}