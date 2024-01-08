package com.funny.translation

import com.funny.translation.kmp.KMPActivity
import com.funny.translation.kmp.KMPContext

actual class WebViewActivity : KMPActivity {
    actual companion object {
        actual fun start(context: KMPContext, url: String) {
        }
    }
}