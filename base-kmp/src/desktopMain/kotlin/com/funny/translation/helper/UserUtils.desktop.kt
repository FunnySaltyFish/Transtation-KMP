package com.funny.translation.helper

import com.funny.translation.kmp.KMPContext

actual suspend fun UserUtils.uploadUserAvatar(
    context: KMPContext,
    imgUri: String,
    filename: String,
    width: Int,
    height: Int,
    uid: Int
): String {
    TODO("Not yet implemented")
}