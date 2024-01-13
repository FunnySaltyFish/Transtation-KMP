package com.funny.translation.helper
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

private const val TAG = "BitmapUtil"
actual object BitmapUtil {
    actual fun compressImage(bytes: ByteArray?, width: Int, height: Int, maxSize: Long): ByteArray {
        bytes ?: return byteArrayOf()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val buffer = ByteBuffer.wrap(bytes)
        buffer.position(0)
        image.copyPixelsFromBuffer(buffer)
        return compressImage(image, maxSize)
    }

    actual fun getBitmapFromUri(ctx: Context, targetWidth: Int, targetHeight: Int, maxSize: Long, uri: Uri): ByteArray? {
        val (originalWidth, originalHeight) = getImageSizeFromUri(ctx, uri)
        if (originalWidth == -1 || originalHeight == -1) return null
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        var be = 1 // be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > targetWidth) { //如果宽度大的话根据宽度固定大小缩放
            be = (originalWidth / targetWidth)
        } else if (originalWidth < originalHeight && originalHeight > targetHeight) { //如果高度高的话根据宽度固定大小缩放
            be = (originalHeight / targetHeight)
        }
        if (be <= 0) be = 1
        //比例压缩
        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inSampleSize = be //设置缩放比例
        bitmapOptions.inDither = true //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
        ctx.contentResolver.openInputStream(uri.toAndroidUri())?.use {
            return compressImage(BitmapFactory.decodeStream(it, null, bitmapOptions), maxSize) //再进行质量压缩
        }
        return null
    }

    // 获取图片的宽高，如果获取失败则返回 -1, -1
    actual fun getImageSizeFromUri(ctx: Context, uri: Uri): Pair<Int, Int> {
        Log.d(TAG, "getImageSizeFromUri: $uri, toAndroidUri: ${uri.toAndroidUri()}")
        val input = ctx.contentResolver.openInputStream(uri.toAndroidUri())
        input?.use {
            val onlyBoundsOptions = BitmapFactory.Options()
            onlyBoundsOptions.inJustDecodeBounds = true
            onlyBoundsOptions.inDither = true //optional
            onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions)
            val originalWidth = onlyBoundsOptions.outWidth
            val originalHeight = onlyBoundsOptions.outHeight
            return Pair(originalWidth, originalHeight)
        }
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

    fun getBitmapFromResources(
        re: Resources?,
        id: Int
    ): Bitmap {
        return BitmapFactory.decodeResource(re, id)
    }

    @JvmStatic
    fun getBitmapFromResources(
        re: Resources?,
        id: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap {
        return getScaledBitmap(BitmapFactory.decodeResource(re, id), targetWidth, targetHeight)
    }

    fun getScaledBitmap(
        bitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val scaleWidth = targetWidth.toFloat() / width
        val scaleHeight = targetHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    fun getBigBitmapFromResources(
        re: Resources?,
        id: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(re, id, options)
        //现在原始宽高以存储在了options对象的outWidth和outHeight实例域中
        val rawWidth = options.outWidth
        val rawHeight = options.outHeight
        var inSampleSize = 1
        if (rawWidth > targetWidth || rawHeight > targetHeight) {
            val ratioHeight = rawHeight.toFloat() / targetHeight
            val ratioWidth = rawWidth.toFloat() / targetWidth
            inSampleSize = Math.min(ratioWidth, ratioHeight).toInt()
        }
        options.inSampleSize = inSampleSize
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(re, id, options)
    }

    /**
     * 质量压缩方法
     *
     * @param image
     * @return
     */
    fun compressImage(image: Bitmap?, maxSize: Long): ByteArray {
        val baos = ByteArrayOutputStream()
        image!!.compress(Bitmap.CompressFormat.JPEG, 100, baos) //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options = 100
        var bytes: ByteArray
        while (baos.toByteArray()
                .also { bytes = it }.size > maxSize
        ) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset() //重置baos即清空baos
            options -= 10 //每次都减少10
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差 ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos) //这里压缩options%，把压缩后的数据存放到baos中
        }
        image.recycle()
        return bytes
    }

}