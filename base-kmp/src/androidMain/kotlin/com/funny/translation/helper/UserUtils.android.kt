package com.funny.translation.helper

import com.eygraber.uri.Uri
import com.funny.translation.kmp.KMPContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

actual suspend fun UserUtils.uploadUserAvatar(
    context: KMPContext,
    imgUri: Uri,
    filename: String,
    width: Int,
    height: Int,
    uid: Int
): String {
    try {
        val data = BitmapUtil.getBitmapFromUri(context, TARGET_AVATAR_SIZE, TARGET_AVATAR_SIZE, 1024 * 100, imgUri)
            ?: return ""
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("uid", uid.toString())
            .addFormDataPart("avatar", filename, data.toRequestBody())
            .build()
        val response = userService.uploadAvatar(body)
        if (response.code == 50){
            return response.data ?: ""
        }
    }catch (e: Exception){
        e.printStackTrace()
    }

    return ""
}