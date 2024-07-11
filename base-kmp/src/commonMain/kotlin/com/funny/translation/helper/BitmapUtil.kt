package com.funny.translation.helper

expect object BitmapUtil {
    fun compressImage(bytes: ByteArray?, maxWidth: Int, maxHeight: Int, maxSize: Long): ByteArray
    fun getBitmapFromUri(uri: String): ByteArray?
    fun getBitmapFromUri(ctx: Context, targetWidth: Int, targetHeight: Int, maxSize: Long, uri: String): ByteArray?
    fun getImageSizeFromUri(ctx: Context, uri: String): Pair<Int, Int>
    fun saveBitmap(bytes: ByteArray, imagePath: String)
}
