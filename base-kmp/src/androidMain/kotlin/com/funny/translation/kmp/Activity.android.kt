package com.funny.translation.kmp

import androidx.appcompat.app.AppCompatActivity
import com.funny.translation.helper.toastOnUi

actual typealias KMPActivity = AppCompatActivity

fun KMPActivity.toastOnUi(msg: String) {
    appCtx.toastOnUi(msg)
}