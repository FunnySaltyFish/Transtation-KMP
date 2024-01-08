package com.funny.translation.helper

import com.eygraber.uri.Uri

expect object BitmapUtil {
    fun compressImage(bytes: ByteArray?, width: Int, height: Int, maxSize: Long): ByteArray
    fun getBitmapFromUri(ctx: Context, targetWidth: Int, targetHeight: Int, maxSize: Long, uri: Uri): ByteArray?
    fun getImageSizeFromUri(ctx: Context, uri: Uri): Pair<Int, Int>
    fun saveBitmap(bytes: ByteArray, imagePath: String)
}
