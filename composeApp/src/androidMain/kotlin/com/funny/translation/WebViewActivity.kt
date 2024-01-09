package com.funny.translation

import com.funny.translation.helper.Context
import com.funny.translation.kmp.KMPActivity

actual class WebViewActivity : KMPActivity() {
    actual companion object {
        actual fun start(context: Context, url: String) {
        }
    }
}