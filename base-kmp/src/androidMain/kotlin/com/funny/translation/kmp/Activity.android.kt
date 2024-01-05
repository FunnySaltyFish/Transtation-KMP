package com.funny.translation.kmp

import com.funny.translation.helper.toastOnUi

actual interface KMPActivity

fun KMPActivity.toastOnUi(msg: String) {
    appCtx.toastOnUi(msg)
}